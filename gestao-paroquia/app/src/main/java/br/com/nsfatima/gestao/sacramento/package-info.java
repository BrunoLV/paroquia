/**
 * Contexto de sacramentos: escrituração dos assentos de Batismo, Confirmação, Matrimônio e Ordem
 * Sacra com fé pública canônica (Cân. 535).
 *
 * <p>O agregado {@code AssentoSacramental} percorre {@code DRAFT -> SEALED} (homologado pelo Pároco)
 * ou {@code CANCELADO}. O teor documental é um snapshot textual dos participantes; o vínculo com
 * {@code Fiel}/{@code Clerigo} é ponteiro opcional e tardio (late binding) que nunca altera o texto
 * lavrado. Alterações de estado canônico do batizado entram como averbações append-only no assento
 * de Batismo; correções materiais entram como ressalvas, sem sobrescrever o original. A Penitência
 * jamais é registrada (Cân. 983).</p>
 */
package br.com.nsfatima.gestao.sacramento;
