package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.application.service.EventoIdempotencyService;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.domain.policy.BloqueioAnoParoquialPolicy;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.application.service.ValidadorVinculoProjeto;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** O ano paroquial do evento e o do dia civil de Brasilia, nao o do instante em UTC. */
@ExtendWith(MockitoExtension.class)
class CriarEventoUseCaseFusoTest {

    private static final RelogioNegocio RELOGIO = new RelogioNegocio(
            Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC), ZoneId.of("America/Sao_Paulo"));

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
    private EventoIdempotencyService idempotencyService;

    private CriarEventoRequest requestEm(Instant inicio) {
        return new CriarEventoRequest("Vigilia", "Descricao", null, UUID.randomUUID(), null, null, List.of(), null,
                inicio, inicio.plusSeconds(3600), null, null, null, null, null, null, null,
                TipoHorarioEvento.PERSONALIZADO, FormatoEvento.ONLINE);
    }

    private void executarAteBloqueioDoAno(Instant inicio) {
        CriarEventoUseCase useCase = new CriarEventoUseCase(
                domainService, null, null, null, idempotencyService, null, null, null, actorContextResolver,
                null, null, validadorVinculoProjeto, bloqueioAnoParoquialPolicy, anoParoquialRepository,
                null, null, null, null, null, null, null, null, RELOGIO);
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "PAROCO", "PAROQUIA", null, null, null));
        when(idempotencyService.findExisting(any(), any())).thenReturn(Optional.empty());
        doThrow(new ViolacaoRegraNegocioException("ano bloqueado"))
                .when(bloqueioAnoParoquialPolicy).assertPermitido(any(), any(), any());

        assertThatThrownBy(() -> useCase.execute("idemp", requestEm(inicio)))
                .isInstanceOf(ViolacaoRegraNegocioException.class);
    }

    @Test
    @DisplayName("Evento as 22h de 31/12 em Brasilia (01h UTC de 01/01) pertence ao ano paroquial que termina")
    void eventoNaViradaDoAnoPertenceAoAnoQueTermina() {
        executarAteBloqueioDoAno(Instant.parse("2027-01-01T01:00:00Z"));

        verify(anoParoquialRepository).findByAno(2026);
        verify(anoParoquialRepository, never()).findByAno(2027);
    }

    @Test
    @DisplayName("Evento as 00h30 de 01/01 em Brasilia (03h30 UTC) pertence ao ano novo")
    void eventoLogoAposAViradaPertenceAoAnoNovo() {
        executarAteBloqueioDoAno(Instant.parse("2027-01-01T03:30:00Z"));

        verify(anoParoquialRepository).findByAno(2027);
        verify(anoParoquialRepository, never()).findByAno(2026);
    }
}
