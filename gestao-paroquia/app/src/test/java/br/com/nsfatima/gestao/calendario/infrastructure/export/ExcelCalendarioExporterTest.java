package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.CalendarioExporter;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExcelCalendarioExporterTest {

    private final CalendarioExporter exporter = new ExcelCalendarioExporter();

    @Test
    void shouldExportToExcel() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setNome("Paróquia Teste");
        config.setDiocese("Diocese Teste");
        config.setEndereco("Endereço Teste");

        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Missa de Teste Excel");
        event.setInicioUtc(Instant.parse("2026-05-10T10:00:00Z"));
        event.setFimUtc(Instant.parse("2026-05-10T11:00:00Z"));
        event.setStatus("CONFIRMADO");

        byte[] result = exporter.export(
                List.of(event),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Instant.parse("2026-05-01T00:00:00Z"),
                Instant.parse("2026-05-31T23:59:59Z"),
                config
        );

        assertNotNull(result);
        assertTrue(exporter.supports("EXCEL"));
        assertTrue(result.length > 0);
    }
}
