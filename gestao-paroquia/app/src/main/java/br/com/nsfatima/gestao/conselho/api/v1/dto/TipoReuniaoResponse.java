package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.ModoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Set;

/** Tipo de reunião e as regras que o formulário da ata precisa conhecer. */
public record TipoReuniaoResponse(
        TipoReuniao codigo,
        String rotulo,
        boolean exigeHomologacao,
        boolean aplicaQuorum,
        ModoPresenca modoPresenca,
        boolean permiteVotacao,
        boolean exigeOrganizacaoAssociada,
        Set<TipoOrganizacao> tiposConvocantes,
        Set<SubtipoConselho> subtiposConselho,
        Set<PapelOrganizacional> papeisRedatores) {
}
