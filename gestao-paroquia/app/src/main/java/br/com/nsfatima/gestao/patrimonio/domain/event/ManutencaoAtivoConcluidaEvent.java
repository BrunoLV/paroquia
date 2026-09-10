package br.com.nsfatima.gestao.patrimonio.domain.event;

import java.util.UUID;

/**
 * Publicado quando uma manutenção é concluída e o ativo volta a {@code DISPONIVEL}. Dispara o
 * recálculo do status: o bem pode precisar voltar imediatamente a {@code RESERVADO} se houver uma
 * reserva ativa aguardando.
 */
public record ManutencaoAtivoConcluidaEvent(UUID ativoId) {
}
