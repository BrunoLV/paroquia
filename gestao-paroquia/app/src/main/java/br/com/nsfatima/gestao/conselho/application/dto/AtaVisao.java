package br.com.nsfatima.gestao.conselho.application.dto;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Ata como o ator a enxerga: o agregado, as ações que o seu papel permite no estado atual (a interface não
 * replica a regra de autorização) e os nomes resolvidos de pessoas e organizações, que a ata não persiste.
 */
public record AtaVisao(
        AtaReuniao ata,
        Set<AcaoAta> acoes,
        Map<UUID, String> nomesFieis,
        String nomeOrganizacaoConvocante,
        String nomeOrganizacaoAssociada) {

    public AtaVisao {
        acoes = Set.copyOf(acoes);
        nomesFieis = Map.copyOf(nomesFieis);
    }
}
