package br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection;

import java.util.UUID;

/**
 * Quantidade de itens (ex.: aprovações pendentes) agrupada por projeto.
 *
 * @param projetoId projeto de referência
 * @param quantidade itens contados
 */
public record ContagemPorProjeto(UUID projetoId, Long quantidade) {
}
