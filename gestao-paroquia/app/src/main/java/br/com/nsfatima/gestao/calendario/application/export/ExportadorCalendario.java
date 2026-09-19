package br.com.nsfatima.gestao.calendario.application.export;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contrato de estratégia (Strategy pattern) para geração de calendário de eventos
 * em um formato de arquivo específico (ex.: PDF, ICS, CSV). Cada implementação é
 * descoberta pelo Spring via injeção de {@code List<ExportadorCalendario>} e selecionada
 * em tempo de execução por {@code ExportacaoCalendarioUseCase}, que consulta
 * {@link #suporta(String)} para escolher o exportador adequado ao formato solicitado.
 */
public interface ExportadorCalendario {

    /**
     * Indica se este exportador é capaz de gerar o calendário no formato informado.
     *
     * @param formato identificador do formato de exportação (ex.: "PDF", "ICS", "CSV", "EXCEL")
     * @return {@code true} se este exportador trata o formato informado
     */
    boolean suporta(String formato);

    /**
     * Gera o conteúdo binário do calendário exportado, resolvendo nomes de locais e
     * organizações a partir dos mapas auxiliares fornecidos (evitando N+1 consultas
     * por evento) e aplicando a configuração da paróquia ao layout do documento.
     *
     * @param eventos eventos já filtrados pelo período e critérios da exportação
     * @param localMap mapa de id de local para nome, usado para resolução de exibição
     * @param orgMap mapa de id de organização para nome, usado para resolução de exibição
     * @param start início do período coberto pela exportação
     * @param end fim do período coberto pela exportação
     * @param config configuração da paróquia usada para personalizar o documento gerado
     * @return conteúdo binário do arquivo exportado no formato suportado
     */
    byte[] exportar(List<EventoEntity> eventos,
                    Map<UUID, String> localMap,
                    Map<UUID, String> orgMap,
                    Instant start,
                    Instant end,
                    ParoquiaConfiguracaoEntity config);
}
