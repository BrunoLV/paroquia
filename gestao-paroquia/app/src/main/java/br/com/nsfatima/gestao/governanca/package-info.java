/**
 * Contexto de governança: ciclo de vida do Ano Paroquial ({@code AnoParoquialEntity}/
 * {@code AnoParoquialStatus}, {@code EM_PLANEJAMENTO -> FECHADO -> CONCLUIDO}), que gate-keeps o
 * provisionamento anual de solenidades/eventos para Matriz e Comunidades ({@code AnoParoquialSeeder})
 * e sua transição automática/agendada entre anos ({@code AnoParoquialRolloverService}).
 *
 * <p>Um Ano Paroquial fechado impede a criação regular de eventos dentro dele — eventos criados
 * fora desse provisionamento entram como {@code ADICIONADO_EXTRA} no contexto {@code calendario}.
 */
package br.com.nsfatima.gestao.governanca;
