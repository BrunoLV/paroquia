package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Ata completa. {@code acoes} traz o que o ator pode fazer no estado atual, calculado pelo servidor, para que a
 * interface não replique a regra de autorização. {@code numero} é nulo enquanto a ata é minuta.
 */
public record AtaResponse(
        UUID id,
        TipoReuniao tipo,
        String tipoRotulo,
        UUID organizacaoConvocanteId,
        String organizacaoConvocante,
        UUID organizacaoAssociadaId,
        String organizacaoAssociada,
        StatusAta status,
        String numero,
        ConteudoAtaResponse conteudo,
        QuorumResponse quorum,
        String redator,
        String homologador,
        String motivoVeto,
        Instant decididoEmUtc,
        UUID ataRetificadaId,
        String justificativaRetificacao,
        Instant criadoEmUtc,
        Set<AcaoAta> acoes) {
}
