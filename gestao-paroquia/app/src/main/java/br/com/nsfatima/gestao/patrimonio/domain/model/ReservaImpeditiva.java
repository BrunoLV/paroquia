package br.com.nsfatima.gestao.patrimonio.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Projeção read-only de uma reserva que impede uma operação sobre o ativo (envio para manutenção
 * ou exclusão). É um conceito do domínio de patrimônio traduzido pela camada ACL a partir do
 * módulo de Reservas — patrimônio não conhece o agregado {@code Reserva}.
 */
public record ReservaImpeditiva(
        UUID reservaId,
        String descricao,
        Instant inicio,
        Instant fim,
        String status,
        String solicitanteNome) {
}
