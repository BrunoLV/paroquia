package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.ExportadorCalendario;
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

/**
 * Exporta uma lista de eventos do calendário para o formato CSV, delimitado por ponto e vírgula
 * (compatível com o Excel em localidade pt-BR) e com BOM UTF-8 para preservar acentuação.
 */
@Component
public class ExportadorCalendarioCsv implements ExportadorCalendario {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String DOUBLE_QUOTE = "\"";

    /**
     * Indica se este exportador atende ao formato solicitado.
     *
     * @param formato identificador do formato de exportação (ex.: {@code "CSV"})
     * @return {@code true} se o formato, ignorando caixa, for "CSV"
     */
    @Override
    public boolean suporta(String formato) {
        return "CSV".equalsIgnoreCase(formato);
    }

    /**
     * Gera o conteúdo CSV com uma linha de cabeçalho e uma linha por evento, resolvendo nomes de
     * local e organização a partir dos mapas fornecidos e convertendo os instantes UTC para o
     * fuso horário de São Paulo antes de formatá-los.
     *
     * @param eventos eventos a serem exportados
     * @param localMap mapa de ID de local para nome de exibição
     * @param orgMap mapa de ID de organização para nome de exibição
     * @param start início do período exportado (não utilizado diretamente no corpo do CSV)
     * @param end fim do período exportado (não utilizado diretamente no corpo do CSV)
     * @param config configuração da paróquia (não utilizada nesta exportação)
     * @return bytes do arquivo CSV, codificados em UTF-8 com BOM
     */
    @Override
    public byte[] exportar(List<EventoEntity> eventos,
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
                    .append(escapeCsv(orgMap.getOrDefault(e.getGrupoParoquialResponsavelId(), "N/A"))).append(";")
                    .append(escapeCsv(e.getStatus())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Formata um instante UTC como data/hora no fuso de São Paulo, no padrão {@code dd/MM/yyyy HH:mm}.
     *
     * @param instant instante a formatar, pode ser {@code null}
     * @return string formatada, ou vazia se o instante for {@code null}
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DATE_FORMATTER);
    }

    /**
     * Escapa um valor para uso seguro em um campo CSV delimitado por ponto e vírgula, duplicando
     * aspas duplas e envolvendo o valor entre aspas quando contém delimitador, aspas ou quebras
     * de linha.
     *
     * @param val valor a escapar, pode ser {@code null}
     * @return valor escapado pronto para ser inserido na linha CSV, ou vazio se {@code null}
     */
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
