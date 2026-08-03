package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.util.UUID;

/**
 * Representa a regra de recorrência associada a uma série de eventos gerados a partir de um evento
 * base, expondo apenas os dados essenciais para identificação da série (frequência e intervalo).
 *
 * @param eventoBaseId identificador do evento original a partir do qual a série de recorrência foi
 *     gerada
 * @param frequencia nome da frequência de recorrência (ex: "SEMANAL", "MENSAL", "ANUAL")
 * @param intervalo intervalo entre ocorrências na unidade da frequência (ex: 2 com frequência
 *     SEMANAL significa "a cada 2 semanas")
 */
public record EventoRecorrenciaResponse(
        UUID id,
        UUID eventoBaseId,
        String frequencia,
        int intervalo) {
}
