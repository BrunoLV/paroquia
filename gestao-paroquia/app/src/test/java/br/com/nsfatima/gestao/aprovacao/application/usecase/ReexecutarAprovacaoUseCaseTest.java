package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecisaoAprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.service.AprovacaoRecalculoConflitoService;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.domain.model.TipoFalhaAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReexecutarAprovacaoUseCaseTest {

    @Mock
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    private AprovacaoMapper aprovacaoMapper;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private ValidarAprovacaoUseCase validarAprovacaoUseCase;

    @Mock
    private AprovacaoAuditPublisher auditPublisher;

    @Mock
    private AprovacaoMetricsPublisher metricsPublisher;

    @Mock
    private EstrategiaExecucaoAprovacao strategy;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AprovacaoRecalculoConflitoService recalculoConflitoService;

    private ReexecutarAprovacaoUseCase useCase;

    private UUID aprovacaoId;
    private AprovacaoEntity entity;

    @BeforeEach
    void setUp() {
        aprovacaoMapper = new AprovacaoMapper(Mockito.mock(LegacyEnumInconsistencyPublisher.class));

        useCase = new ReexecutarAprovacaoUseCase(
                aprovacaoJpaRepository,
                aprovacaoMapper,
                actorContextResolver,
                validarAprovacaoUseCase,
                auditPublisher,
                metricsPublisher,
                List.of(strategy),
                eventPublisher,
                recalculoConflitoService
        );

        aprovacaoId = UUID.randomUUID();
        entity = new AprovacaoEntity();
        entity.setId(aprovacaoId);
        entity.setStatus(AprovacaoStatus.FALHA_EXECUCAO);
        entity.setTipoFalhaExecucao(TipoFalhaAprovacao.SISTEMICA_APLICACAO);
        entity.setMensagemErroExecucao("Timeout de conexão");
        entity.setDetalheErroExecucao("SocketTimeoutException: Read timed out");
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        entity.setAprovadorPapel("PAROCO");
        entity.setSolicitanteId("coordenador-1");
        entity.setCriadoEmUtc(Instant.now());

        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("paroco-1", "paroco-1", "PAROCO", null, null, null));
    }

    @Test
    @DisplayName("Deve reexecutar com sucesso aprovação com falha sistêmica")
    void deveReexecutarComSucessoFalhaSistemica() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(eq(entity), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        DecisaoAprovacaoResponse response = useCase.reexecutar(aprovacaoId);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("APROVADA");
        assertThat(response.actionExecution().outcome()).isEqualTo("EXECUTED");

        assertThat(entity.getStatus()).isEqualTo(AprovacaoStatus.APROVADA);
        assertThat(entity.getMensagemErroExecucao()).isNull();
        assertThat(entity.getTipoFalhaExecucao()).isNull();
        assertThat(entity.getDetalheErroExecucao()).isNull();

        verify(aprovacaoJpaRepository).save(entity);
        verify(metricsPublisher).publishAprovacaoFlow("CRIACAO_EVENTO", "REEXECUTED_SUCCESS");
    }

    @Test
    @DisplayName("NÃO deve permitir reexecução de aprovação com falha cadastral ou conflito de negócio")
    void naoDevePermitirReexecucaoDeFalhaCadastral() {
        entity.setTipoFalhaExecucao(TipoFalhaAprovacao.NEGOCIO_CADASTRAL);
        entity.setMensagemErroExecucao("Conflito de espaço físico");
        entity.setDetalheErroExecucao("Salão São José já reservado");

        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> useCase.reexecutar(aprovacaoId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Solicitações com falha cadastral ou de negócio são imutáveis e não admitem reexecução direta");
    }

    @Test
    @DisplayName("Se nova tentativa de reexecução falhar, deve atualizar detalhes do erro e manter FALHA_EXECUCAO")
    void deveAtualizarErroQuandoReexecucaoFalhar() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(eq(entity), any())).thenThrow(new RuntimeException("Serviço indisponível novamente"));

        assertThatThrownBy(() -> useCase.reexecutar(aprovacaoId))
                .isInstanceOf(AprovacaoExecucaoFalhouException.class)
                .hasMessageContaining("Falha na reexecução");

        assertThat(entity.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(entity.getTipoFalhaExecucao()).isEqualTo(TipoFalhaAprovacao.SISTEMICA_APLICACAO);
        assertThat(entity.getMensagemErroExecucao()).isEqualTo("Serviço indisponível novamente");

        verify(metricsPublisher).publishAprovacaoFlow("CRIACAO_EVENTO", "REEXECUTED_FAILED");
    }
}
