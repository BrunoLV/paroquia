package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.ExportadorCalendario;
import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportadorCalendarioPptxTest {

    private final ExportadorCalendario exportador = new ExportadorCalendarioPptx();

    @Test
    void deveExportarParaPowerPoint() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setNome("Paróquia Teste");
        config.setCorPrimariaHex("#1A237E");

        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Missa de Teste PowerPoint");
        event.setCategoria(CategoriaEvento.LITURGICO);
        event.setInicioUtc(Instant.parse("2026-05-10T10:00:00Z"));
        event.setFimUtc(Instant.parse("2026-05-10T11:00:00Z"));
        event.setStatus("CONFIRMADO");
        event.setEstadoConflito(EstadoConflitoAgenda.PENDENTE);
        event.setMotivoConflito("Sobreposição com outro evento no mesmo salão");
        event.setAdicionadoExtraJustificativa("Autorizado pelo pároco para atender demanda extraordinária");

        byte[] result = exportador.exportar(
                List.of(event),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-05-31T23:59:59Z"),
                config,
                "AGENDA"
        );

        assertNotNull(result);
        assertTrue(exportador.suporta("POWERPOINT"));
        assertTrue(result.length > 0);
    }

    @Test
    void deveExportarEmModoGrade() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setNome("Paróquia Teste");

        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Missa de Teste Grade");
        event.setCategoria(CategoriaEvento.PASTORAL);
        event.setInicioUtc(Instant.parse("2026-05-10T10:00:00Z"));
        event.setFimUtc(Instant.parse("2026-05-10T11:00:00Z"));
        event.setStatus("CONFIRMADO");

        byte[] result = exportador.exportar(
                List.of(event),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-05-31T23:59:59Z"),
                config,
                "GRADE"
        );

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void deveExportarSemEventos() {
        byte[] result = exportador.exportar(
                List.of(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-05-31T23:59:59Z"),
                null,
                "AGENDA"
        );

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
