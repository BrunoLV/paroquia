package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionRequest;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
class AprovacaoExecutionResilienceTest {

    private AprovacaoJpaRepository repository;
    private EventoActorContextResolver actorContextResolver;
    private DecideSolicitacaoAprovacaoUseCase decideUseCase;
    private List<EstrategiaExecucaoAprovacao> strategies;

    @BeforeEach
    void setUp() {
        repository = mock(AprovacaoJpaRepository.class);
        actorContextResolver = mock(EventoActorContextResolver.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        ValidateAprovacaoUseCase validateAprovacaoUseCase = mock(ValidateAprovacaoUseCase.class);
        AprovacaoAcaoPayloadMapper payloadMapper = mock(AprovacaoAcaoPayloadMapper.class);
        AprovacaoAuditPublisher auditPublisher = mock(AprovacaoAuditPublisher.class);
        AprovacaoMetricsPublisher metricsPublisher = mock(AprovacaoMetricsPublisher.class);

        EstrategiaExecucaoAprovacao createStrategy = mock(EstrategiaExecucaoAprovacao.class);
        when(createStrategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);

        EstrategiaExecucaoAprovacao updateStrategy = mock(EstrategiaExecucaoAprovacao.class);
        when(updateStrategy.supports(TipoSolicitacaoInput.EDICAO_EVENTO)).thenReturn(true);

        EstrategiaExecucaoAprovacao cancelStrategy = mock(EstrategiaExecucaoAprovacao.class);
        when(cancelStrategy.supports(TipoSolicitacaoInput.CANCELAMENTO)).thenReturn(true);

        strategies = List.of(createStrategy, updateStrategy, cancelStrategy);

        AprovacaoMapper aprovacaoMapper = new AprovacaoMapper(mock(LegacyEnumInconsistencyPublisher.class));

        decideUseCase = new DecideSolicitacaoAprovacaoUseCase(
                repository,
                aprovacaoMapper,
                actorContextResolver,
                validateAprovacaoUseCase,
                payloadMapper,
                auditPublisher,
                metricsPublisher,
                strategies,
                eventPublisher);
    }

    @Test
    @DisplayName("Deve marcar aprovação como falha na execução se a estratégia lançar exceção")
    void deveMarcarFalhaNaExecucao() {
        UUID id = UUID.randomUUID();
        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(id);
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CANCELAMENTO.name());
        entity.setStatus(AprovacaoStatus.PENDENTE);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired()).thenReturn(new EventoActorContext("admin",
                "admin", "admin", null, null, null));


        EstrategiaExecucaoAprovacao strategy = strategies.get(2); // cancel
        when(strategy.execute(any(), any())).thenThrow(new RuntimeException("Crash"));

        assertThrows(AprovacaoExecucaoFalhouException.class, () -> decideUseCase.decide(id,
                new AprovacaoDecisionRequest(AprovacaoStatus.APROVADA, "Ready to go")));

        assertEquals(AprovacaoStatus.FALHA_EXECUCAO, entity.getStatus());
        assertTrue(entity.getMensagemErroExecucao().contains("Crash"));
        verify(repository).save(entity);
    }
}
