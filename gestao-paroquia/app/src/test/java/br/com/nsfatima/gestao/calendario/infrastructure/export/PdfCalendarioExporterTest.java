package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.CalendarioExporter;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfCalendarioExporterTest {

    private final CalendarioExporter exporter = new PdfCalendarioExporter();

    @Test
    void deveExportarParaPdf() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setNome("Paróquia Teste");
        config.setDiocese("Diocese Teste");
        config.setEndereco("Endereço Teste");
        config.setCorPrimariaHex("#1A237E");
        config.setCorSecundariaHex("#808080");

        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Missa de Teste PDF");
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
        assertTrue(exporter.supports("PDF"));
        assertTrue(result.length > 0);
    }
}
