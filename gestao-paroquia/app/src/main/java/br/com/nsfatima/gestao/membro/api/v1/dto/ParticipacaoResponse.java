package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ParticipacaoResponse(
        UUID id,
        UUID membroId,
        UUID organizacaoId,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativo) {
}
