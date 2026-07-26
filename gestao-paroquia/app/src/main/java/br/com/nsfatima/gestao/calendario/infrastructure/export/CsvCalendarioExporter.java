package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.CalendarioExporter;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CsvCalendarioExporter implements CalendarioExporter {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String DOUBLE_QUOTE = "\"";

    @Override
    public boolean supports(String formato) {
        return "CSV".equalsIgnoreCase(formato);
    }

    @Override
    public byte[] export(List<EventoEntity> eventos,
                         Map<UUID, String> localMap,
                         Map<UUID, String> orgMap,
                         Instant start,
                         Instant end,
                         ParoquiaConfiguracaoEntity config) {
        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM para garantir acentos corretos no Excel em português
        sb.append('\uFEFF');

        // Cabeçalhos
        sb.append("Título;Descrição;Categoria;Início;Fim;Local;Organização Responsável;Status\n");

        // Linhas
        for (EventoEntity e : eventos) {
            sb.append(escapeCsv(e.getTitulo())).append(";")
                    .append(escapeCsv(e.getDescricao())).append(";")
                    .append(escapeCsv(e.getCategoria())).append(";")
                    .append(formatInstant(e.getInicioUtc())).append(";")
                    .append(formatInstant(e.getFimUtc())).append(";")
                    .append(escapeCsv(localMap.getOrDefault(e.getLocalId(), "N/A"))).append(";")
                    .append(escapeCsv(orgMap.getOrDefault(e.getOrganizacaoResponsavelId(), "N/A"))).append(";")
                    .append(escapeCsv(e.getStatus())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DATE_FORMATTER);
    }

    private String escapeCsv(Object val) {
        if (val == null) {
            return "";
        }
        String s = val.toString().replace(DOUBLE_QUOTE, "\"\"");
        if (s.contains(";") || s.contains("\n") || s.contains("\r") || s.contains(DOUBLE_QUOTE)) {
            return DOUBLE_QUOTE + s + DOUBLE_QUOTE;
        }
        return s;
    }
}
