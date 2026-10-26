package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.application.export.ExportadorCalendario;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Mes e ano da exportacao sao dias civis de Brasilia, convertidos em instantes UTC. */
class ExportacaoCalendarioUseCaseFusoTest {

    private EventoJpaRepository eventoRepository;
    private ExportadorCalendario exportador;

    @BeforeEach
    void setUp() {
        eventoRepository = mock(EventoJpaRepository.class);
        exportador = mock(ExportadorCalendario.class);
        when(exportador.suporta(anyString())).thenReturn(true);
        Page<EventoEntity> vazia =
                new PageImpl<>(List.of());
        when(eventoRepository.findAllWithFilters(any(), anyBoolean(), any(), anyBoolean(), any(), anyBoolean(),
                any(), anyBoolean(), any(), anyBoolean(), any(), anyBoolean(), any(), anyBoolean(),
                any(Pageable.class))).thenReturn(vazia);
    }

    private ExportacaoCalendarioUseCase useCaseEm(String agoraUtc) {
        RelogioNegocio relogio = new RelogioNegocio(
                Clock.fixed(Instant.parse(agoraUtc), ZoneOffset.UTC), ZoneId.of("America/Sao_Paulo"));
        return new ExportacaoCalendarioUseCase(eventoRepository, mock(LocalJpaRepository.class),
                mock(OrganizacaoJpaRepository.class), mock(ProjetoEventoJpaRepository.class),
                mock(ParoquiaConfiguracaoRepository.class), List.of(exportador), relogio);
    }

    private Instant[] periodoExportado() {
        ArgumentCaptor<Instant> inicio = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> fim = ArgumentCaptor.forClass(Instant.class);
        verify(eventoRepository).findAllWithFilters(inicio.capture(), anyBoolean(), fim.capture(), anyBoolean(),
                any(), anyBoolean(), any(), anyBoolean(), any(), anyBoolean(), any(), anyBoolean(), any(),
                anyBoolean(), any(Pageable.class));
        return new Instant[] {inicio.getValue(), fim.getValue()};
    }

    @Test
    @DisplayName("Mes corrente as 22h BRT do ultimo dia (01h UTC do mes seguinte) ainda e o mes que termina")
    void mesCorrenteNoUltimoDiaApos21h() {
        useCaseEm("2026-04-01T01:30:00Z").execute("PDF", "MES", null, null, null, null, null, null, "AGENDA");

        Instant[] periodo = periodoExportado();
        assertThat(periodo[0]).isEqualTo(Instant.parse("2026-03-01T03:00:00Z"));
        assertThat(periodo[1]).isEqualTo(Instant.parse("2026-04-01T02:59:59.999999999Z"));
    }

    @Test
    @DisplayName("Fevereiro de ano bissexto cobre 29/02 inteiro no fuso de Brasilia")
    void fevereiroBissexto() {
        useCaseEm("2026-04-01T12:00:00Z").execute("PDF", "MES", 2028, 2, null, null, null, null, "AGENDA");

        Instant[] periodo = periodoExportado();
        assertThat(periodo[0]).isEqualTo(Instant.parse("2028-02-01T03:00:00Z"));
        assertThat(periodo[1]).isEqualTo(Instant.parse("2028-03-01T02:59:59.999999999Z"));
    }

    @Test
    @DisplayName("Ano corrente as 22h BRT de 31/12 ainda e o ano que termina")
    void anoCorrenteNaViradaDoAno() {
        useCaseEm("2027-01-01T01:00:00Z").execute("PDF", "ANO", null, null, null, null, null, null, "AGENDA");

        Instant[] periodo = periodoExportado();
        assertThat(periodo[0]).isEqualTo(Instant.parse("2026-01-01T03:00:00Z"));
        assertThat(periodo[1]).isEqualTo(Instant.parse("2027-01-01T02:59:59.999999999Z"));
    }
}
