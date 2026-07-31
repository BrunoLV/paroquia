package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionRequest;
import br.com.nsfatima.gestao.aprovacao.application.mapper.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.port.out.ApprovalAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.ApprovalMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.ApprovalExecutionStrategy;
import br.com.nsfatima.gestao.aprovacao.domain.exception.ApprovalExecutionFailedException;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class ApprovalExecutionResilienceTest {

    private AprovacaoJpaRepository repository;
    private EventoActorContextResolver actorContextResolver;
    private DecideSolicitacaoAprovacaoUseCase decideUseCase;
    private List<ApprovalExecutionStrategy> strategies;

    @BeforeEach
    void setUp() {
        repository = mock(AprovacaoJpaRepository.class);
        actorContextResolver = mock(EventoActorContextResolver.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        ValidateAprovacaoUseCase validateAprovacaoUseCase = mock(ValidateAprovacaoUseCase.class);
        ApprovalActionPayloadMapper payloadMapper = mock(ApprovalActionPayloadMapper.class);
        ApprovalAuditPublisher auditPublisher = mock(ApprovalAuditPublisher.class);
        ApprovalMetricsPublisher metricsPublisher = mock(ApprovalMetricsPublisher.class);

        ApprovalExecutionStrategy createStrategy = mock(ApprovalExecutionStrategy.class);
        when(createStrategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);

        ApprovalExecutionStrategy updateStrategy = mock(ApprovalExecutionStrategy.class);
        when(updateStrategy.supports(TipoSolicitacaoInput.EDICAO_EVENTO)).thenReturn(true);

        ApprovalExecutionStrategy cancelStrategy = mock(ApprovalExecutionStrategy.class);
        when(cancelStrategy.supports(TipoSolicitacaoInput.CANCELAMENTO)).thenReturn(true);

        strategies = List.of(createStrategy, updateStrategy, cancelStrategy);

        decideUseCase = new DecideSolicitacaoAprovacaoUseCase(
                repository,
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
        entity.setStatus(AprovacaoStatus.PENDENTE.name());

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired()).thenReturn(new EventoActorContext("admin",
                "admin", "admin", null, null, null));


        ApprovalExecutionStrategy strategy = strategies.get(2); // cancel
        when(strategy.execute(any(), any())).thenThrow(new RuntimeException("Crash"));

        assertThrows(ApprovalExecutionFailedException.class, () -> decideUseCase.decide(id,
                new AprovacaoDecisionRequest(AprovacaoStatus.APROVADA, "Ready to go")));

        assertEquals(AprovacaoStatus.FALHA_EXECUCAO.name(), entity.getStatus());
        assertTrue(entity.getMensagemErroExecucao().contains("Crash"));
        verify(repository).save(entity);
    }
}
