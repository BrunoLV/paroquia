package br.com.nsfatima.gestao.reserva.application.event;

import java.util.UUID;

/**
 * Publicado sempre que uma reserva cujo {@code tipoItem} é {@code ATIVO} sofre uma transição de
 * estado relevante (criação confirmada, aprovação, rejeição, cancelamento, retirada, devolução,
 * expiração). Consumido pelo contexto de patrimônio para recalcular o status {@code RESERVADO}
 * do bem. Canal de comunicação assíncrona entre bounded contexts.
 */
public record ReservaDeAtivoAlteradaEvent(UUID ativoId) {
}
