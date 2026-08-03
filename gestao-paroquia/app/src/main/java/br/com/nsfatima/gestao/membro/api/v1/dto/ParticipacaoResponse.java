package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa um registro de participação de um membro em uma organização, incluindo período (início/fim) e
 * situação atual (ativa ou encerrada).
 */
public record ParticipacaoResponse(
        UUID id,
        UUID membroId,
        UUID organizacaoId,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativo) {
}
