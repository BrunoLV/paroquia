package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.domain.model.Aprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link CriarSolicitacaoAprovacaoUseCase}, validando a abertura
 * de solicitações e a emissão das métricas e auditoria associadas.
 */
class CriarSolicitacaoAprovacaoUseCaseTest {

    private AprovacaoJpaRepository repository;
    private EventoActorContextResolver actorContextResolver;
    private EventoAuditPublisher auditPublisher;
    private AprovacaoMapper mapper;
    private AprovacaoMetricsPublisher metricsPublisher;
    private CriarSolicitacaoAprovacaoUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(AprovacaoJpaRepository.class);
        actorContextResolver = mock(EventoActorContextResolver.class);
        auditPublisher = mock(EventoAuditPublisher.class);
        mapper = mock(AprovacaoMapper.class);
        metricsPublisher = mock(AprovacaoMetricsPublisher.class);

        useCase = new CriarSolicitacaoAprovacaoUseCase(
                repository,
                actorContextResolver,
                auditPublisher,
                mapper,
                metricsPublisher);
    }

    @Test
    @DisplayName("Deve persistir solicitação pendente e publicar métrica de solicitação aberta")
    void deveCriarSolicitacaoEPublicarMetrica() {
        UUID eventoId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID aprovacaoId = UUID.randomUUID();

        EventoActorContext actor = new EventoActorContext(
                "secretaria.central",
                "SECRETARIA",
                "PASTORAL",
                null,
                usuarioId,
                null);

        when(actorContextResolver.resolveRequired()).thenReturn(actor);

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(aprovacaoId);
        when(mapper.toEntity(any(Aprovacao.class))).thenReturn(entity);
        when(repository.save(any(AprovacaoEntity.class))).thenReturn(entity);

        AprovacaoResponse response = new AprovacaoResponse(
                aprovacaoId,
                eventoId,
                TipoSolicitacaoResponse.CRIACAO_EVENTO,
                "PENDENTE",
                "PAROCO",
                Instant.now(),
                null,
                usuarioId.toString(),
                null,
                null,
                null);

        when(mapper.toResponse(entity)).thenReturn(response);


        AprovacaoResponse result = useCase.criar(eventoId, TipoSolicitacaoInput.CRIACAO_EVENTO);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(aprovacaoId);
        verify(repository).save(entity);
        verify(auditPublisher).publishCreatePendente("secretaria.central", aprovacaoId.toString(), "N/A");
        verify(metricsPublisher).recordSolicitacaoAberta("CRIACAO_EVENTO", "SECRETARIA");
    }
}
