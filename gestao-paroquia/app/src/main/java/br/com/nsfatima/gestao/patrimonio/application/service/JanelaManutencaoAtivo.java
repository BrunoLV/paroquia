package br.com.nsfatima.gestao.patrimonio.application.service;

import java.time.Instant;

/**
 * Contrato público (module API) de uma janela de manutenção de ativo, consumido pelo módulo de
 * Reservas para bloquear o calendário. {@code emAberto} indica manutenção sem conclusão registrada.
 */
public record JanelaManutencaoAtivo(Instant inicio, Instant fim, boolean emAberto) {
}
