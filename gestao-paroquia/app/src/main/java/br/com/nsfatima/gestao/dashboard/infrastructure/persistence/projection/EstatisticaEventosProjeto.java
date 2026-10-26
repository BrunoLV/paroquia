package br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection;

import java.util.UUID;

/**
 * Contagem de eventos de um projeto: total, já terminados e ainda pendentes (término a partir do
 * instante de referência), obtidas numa única consulta agrupada.
 *
 * @param projetoId projeto ao qual os eventos pertencem
 * @param total eventos vinculados
 * @param realizados eventos cujo término é anterior ao instante de referência
 * @param pendentes eventos cujo término é igual ou posterior ao instante de referência
 */
public record EstatisticaEventosProjeto(UUID projetoId, Long total, Long realizados, Long pendentes) {
}
