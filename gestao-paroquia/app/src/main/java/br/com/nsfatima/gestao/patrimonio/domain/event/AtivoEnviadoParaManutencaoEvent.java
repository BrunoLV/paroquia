package br.com.nsfatima.gestao.patrimonio.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado quando um ativo entra em manutenção. Consumido pela sincronização de status para
 * garantir que o bem saia de {@code RESERVADO} (manutenção tem precedência) e para bloquear o
 * calendário de reservas no intervalo.
 */
public record AtivoEnviadoParaManutencaoEvent(UUID ativoId, Instant inicio, Instant fimPrevista) {
}
