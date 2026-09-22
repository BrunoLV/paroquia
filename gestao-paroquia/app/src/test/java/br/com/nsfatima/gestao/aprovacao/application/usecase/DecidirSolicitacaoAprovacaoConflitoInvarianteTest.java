package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecidirAprovacaoRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecisaoAprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.service.AprovacaoRecalculoConflitoService;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import br.com.nsfatima.gestao.calendario.application.service.DetectorConflitoEventoService;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecidirSolicitacaoAprovacaoConflitoInvarianteTest {

    @Mock
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    private AprovacaoMapper aprovacaoMapper;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private ValidarAprovacaoUseCase validarAprovacaoUseCase;

    @Mock
    private AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    @Mock
    private AprovacaoAuditPublisher auditPublisher;

    @Mock
    private AprovacaoMetricsPublisher metricsPublisher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EstrategiaExecucaoAprovacao strategy;

    @Mock
    private DetectorConflitoEventoService detectorConflitoEventoService;

    @Mock
    private AprovacaoRecalculoConflitoService recalculoConflitoService;

    private DecidirSolicitacaoAprovacaoUseCase useCase;

    private UUID aprovacaoId;
    private AprovacaoEntity entity;
    private AprovacaoAcaoPayload payload;

    @BeforeEach
    void setUp() {
        aprovacaoMapper = new AprovacaoMapper(Mockito.mock(LegacyEnumInconsistencyPublisher.class));

        useCase = new DecidirSolicitacaoAprovacaoUseCase(
                aprovacaoJpaRepository,
                aprovacaoMapper,
                actorContextResolver,
                validarAprovacaoUseCase,
                aprovacaoAcaoPayloadMapper,
                auditPublisher,
                metricsPublisher,
                List.of(strategy),
                eventPublisher,
                detectorConflitoEventoService,
                recalculoConflitoService
        );

        aprovacaoId = UUID.randomUUID();
        entity = new AprovacaoEntity();
        entity.setId(aprovacaoId);
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        entity.setAprovadorPapel("COORD_CONSELHO");
        entity.setSolicitanteId("admin");
        entity.setActionPayloadJson("{\"titulo\": \"Retiro Paroquial\"}");

        payload = new AprovacaoAcaoPayload(
                "idemp-123",
                null,
                "Retiro Paroquial",
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                Instant.parse("2026-10-15T10:00:00Z"),
                Instant.parse("2026-10-15T18:00:00Z"),
                null,
                null,
                null,
                null,
                List.of(),
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                null,
                null,
                null,
                List.of(UUID.randomUUID()),
                null,
                TipoHorarioEvento.PERSONALIZADO,
                FormatoEvento.PRESENCIAL
        );

        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "admin", null, null, null));
    }

    @Test
    @DisplayName("NUNCA deve permitir aprovação se houver conflito de LOCAL com evento já aprovado")
    void deveBloquearAprovacaoQuandoHouverConflitoDeLocal() {
        when(aprovacaoAcaoPayloadMapper.toPayload(entity.getActionPayloadJson())).thenReturn(payload);

        ConflitoEventoItemDto conflitoLocal = new ConflitoEventoItemDto(
                "LOCAL",
                payload.localId(),
                "Salão Paroquial",
                UUID.randomUUID(),
                "Missa Solene",
                payload.inicio(),
                payload.fim(),
                "Padre Pedro",
                1,
                2,
                "Espaço reservado por evento aprovado"
        );
        when(detectorConflitoEventoService.detectarConflitos(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(conflitoLocal));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "Autorizo");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .isInstanceOf(ViolacaoRegraNegocioException.class)
                .hasMessageContaining("Não é permitido aprovar solicitação com conflito de espaço "
                        + "físico ou ativo patrimonial já reservado");

        verify(strategy, never()).execute(any(), any());
        verify(recalculoConflitoService, never()).recalcularConflitosPendentes(any());
    }

    @Test
    @DisplayName("NUNCA deve permitir aprovação se houver conflito de ATIVO patrimonial com evento já aprovado")
    void deveBloquearAprovacaoQuandoHouverConflitoDeAtivo() {
        when(aprovacaoAcaoPayloadMapper.toPayload(entity.getActionPayloadJson())).thenReturn(payload);

        ConflitoEventoItemDto conflitoAtivo = new ConflitoEventoItemDto(
                "ATIVO",
                payload.ativoIds().getFirst(),
                "Projetor Epson",
                UUID.randomUUID(),
                "Missa Solene",
                payload.inicio(),
                payload.fim(),
                "Padre Pedro",
                1,
                2,
                "Ativo já reservado para transmissão"
        );
        when(detectorConflitoEventoService.detectarConflitos(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(conflitoAtivo));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "Autorizo");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .isInstanceOf(ViolacaoRegraNegocioException.class)
                .hasMessageContaining("Não é permitido aprovar solicitação com conflito de espaço "
                        + "físico ou ativo patrimonial já reservado");

        verify(strategy, never()).execute(any(), any());
        verify(recalculoConflitoService, never()).recalcularConflitosPendentes(any());
    }

    @Test
    @DisplayName("Deve aprovar com sucesso e acionar recálculo das pendentes quando não houver conflito de recursos")
    void deveAprovarEAcionarRecalculoQuandoSemConflito() {
        when(aprovacaoAcaoPayloadMapper.toPayload(entity.getActionPayloadJson())).thenReturn(payload);
        when(detectorConflitoEventoService.detectarConflitos(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        DecidirAprovacaoRequest request =
                new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "Autorizado pelo conselho");

        DecisaoAprovacaoResponse response = useCase.decidir(aprovacaoId, request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("APROVADA");
        verify(strategy).execute(any(), any());
        verify(recalculoConflitoService).recalcularConflitosPendentes(aprovacaoId);
    }
}
