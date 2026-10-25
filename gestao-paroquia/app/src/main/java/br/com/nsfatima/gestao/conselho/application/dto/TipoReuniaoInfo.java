package br.com.nsfatima.gestao.conselho.application.dto;

import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniao;

import java.util.Set;
import java.util.UUID;

/**
 * Tipo de reunião e as regras que a interface precisa para montar o formulário (presença, votação, quórum),
 * mais os órgãos pelos quais o usuário autenticado pode redigir ata desse tipo.
 */
public record TipoReuniaoInfo(TipoReuniao tipo, RegrasTipoReuniao regras, Set<UUID> organizacoesRedacao) {

    public TipoReuniaoInfo {
        organizacoesRedacao = Set.copyOf(organizacoesRedacao);
    }

}
