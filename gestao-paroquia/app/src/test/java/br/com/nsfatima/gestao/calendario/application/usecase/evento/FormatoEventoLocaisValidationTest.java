package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.domain.policy.BloqueioAnoParoquialPolicy;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.application.service.ValidadorVinculoProjeto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormatoEventoLocaisValidationTest {

    @Mock
    private EventoDomainService domainService;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private ValidadorVinculoProjeto validadorVinculoProjeto;

    @Mock
    private BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy;

    @Mock
    private AnoParoquialJpaRepository anoParoquialRepository;

    @Mock
    private br.com.nsfatima.gestao.calendario.application.service.EventoIdempotencyService idempotencyService;

    @Test
    @DisplayName("CriarEvento: formato PRESENCIAL sem locais deve lançar ViolacaoRegraNegocioException")
    void presencialSemLocaisDeveLancarExcecao() {
        CriarEventoUseCase useCase = new CriarEventoUseCase(
                domainService, null, null, null, idempotencyService, null, null, null,
                actorContextResolver, null, null, validadorVinculoProjeto,
                bloqueioAnoParoquialPolicy, anoParoquialRepository, null, null, null, null, null, null, null
        );

        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "PAROCO", "PAROQUIA", null, null, null));
        when(idempotencyService.findExisting(any(), any())).thenReturn(java.util.Optional.empty());

        CriarEventoRequest request = new CriarEventoRequest(
                "Retiro Espiritual",
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-10-15T10:00:00Z"),
                Instant.parse("2026-10-15T18:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                TipoHorarioEvento.PERSONALIZADO,
                FormatoEvento.PRESENCIAL
        );

        assertThatThrownBy(() -> useCase.execute("idemp-1", request))
                .isInstanceOf(ViolacaoRegraNegocioException.class)
                .hasMessageContaining("Ao menos um local deve ser informado para eventos presenciais ou híbridos");
    }

    @Test
    @DisplayName("CriarEvento: formato HIBRIDO sem locais deve lançar ViolacaoRegraNegocioException")
    void hibridoSemLocaisDeveLancarExcecao() {
        CriarEventoUseCase useCase = new CriarEventoUseCase(
                domainService, null, null, null, idempotencyService, null, null, null,
                actorContextResolver, null, null, validadorVinculoProjeto,
                bloqueioAnoParoquialPolicy, anoParoquialRepository, null, null, null, null, null, null, null
        );

        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "PAROCO", "PAROQUIA", null, null, null));
        when(idempotencyService.findExisting(any(), any())).thenReturn(java.util.Optional.empty());

        CriarEventoRequest request = new CriarEventoRequest(
                "Palestra Híbrida",
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-10-15T10:00:00Z"),
                Instant.parse("2026-10-15T18:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                TipoHorarioEvento.PERSONALIZADO,
                FormatoEvento.HIBRIDO
        );

        assertThatThrownBy(() -> useCase.execute("idemp-2", request))
                .isInstanceOf(ViolacaoRegraNegocioException.class)
                .hasMessageContaining("Ao menos um local deve ser informado para eventos presenciais ou híbridos");
    }
}
