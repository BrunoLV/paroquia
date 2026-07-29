package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CpcDecisaoResponse(
        UUID id,
        String titulo,
        String pautaDescricao,
        LocalDate dataReuniao,
        UUID comunidadeId,
        String conselhoTipo,
        StatusHomologacao statusHomologacao,
        String solicitanteId,
        String solicitanteNome,
        String homologadorId,
        String motivoVeto,
        Instant criadoEmUtc,
        Instant decididoEmUtc
) {
}
