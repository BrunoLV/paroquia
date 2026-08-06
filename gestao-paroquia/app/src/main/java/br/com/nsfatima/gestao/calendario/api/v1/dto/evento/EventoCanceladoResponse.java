package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.time.Instant;
import java.util.UUID;

/**
 * Resposta retornada após o cancelamento direto e efetivo de um evento (quando não é necessário
 * roteá-lo pelo fluxo de aprovação), confirmando o novo status e preservando o motivo informado
 * para fins de auditoria.
 *
 * @param canceladoMotivo motivo do cancelamento informado pelo solicitante, preservado no evento
 *     cancelado para consulta posterior
 */
public record EventoCanceladoResponse(
        UUID id,
        String status,
        String canceladoMotivo,
        String titulo,
        Instant inicio,
        Instant fim,
        UUID grupoParoquialResponsavelId) {
}
