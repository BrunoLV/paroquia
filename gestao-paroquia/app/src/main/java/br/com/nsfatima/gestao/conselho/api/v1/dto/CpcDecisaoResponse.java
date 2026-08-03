package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representação pública de uma ata/decisão de conselho (CPC/CPP), incluindo os dados de
 * homologação. {@code homologadorId}, {@code motivoVeto} e {@code decididoEmUtc} só são
 * preenchidos após a decisão sair de {@code PENDENTE_HOMOLOGACAO}; {@code motivoVeto} só é
 * relevante quando {@code statusHomologacao} é {@code VETADO}.
 */
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
