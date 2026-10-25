package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.conselho.domain.model.ModoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Objects;
import java.util.Set;

/**
 * O que um tipo de reunião exige. É a resposta da {@link RegrasTipoReuniaoPolicy}: quais organizações podem
 * convocá-lo, se a ata precisa da homologação do Pároco, se aplica quórum, como se registra a presença, se
 * admite votação e quais papéis podem redigir a ata.
 *
 * <p>{@code subtiposConselho} vazio significa "qualquer subtipo" quando {@code CONSELHO} é aceito.</p>
 */
public record RegrasTipoReuniao(
        Set<TipoOrganizacao> tiposConvocantes,
        Set<SubtipoConselho> subtiposConselho,
        boolean exigeOrganizacaoAssociada,
        boolean exigeHomologacao,
        boolean aplicaQuorum,
        ModoPresenca modoPresenca,
        boolean permiteVotacao,
        Set<PapelOrganizacional> papeisRedatores) {

    public RegrasTipoReuniao {
        tiposConvocantes = Set.copyOf(tiposConvocantes);
        subtiposConselho = Set.copyOf(subtiposConselho);
        papeisRedatores = Set.copyOf(papeisRedatores);
    }

    /** Indica se a organização pode convocar uma reunião deste tipo. */
    public boolean aceitaConvocante(ReferenciaOrganizacao organizacao) {
        if (!tiposConvocantes.contains(organizacao.tipo())) {
            return false;
        }
        return organizacao.tipo() != TipoOrganizacao.CONSELHO
                || subtiposConselho.isEmpty()
                || subtiposConselho.contains(organizacao.subtipo());
    }

    /**
     * Indica se a organização pode figurar como segunda organização de uma reunião conjunta: o CAEP da mesma
     * unidade do órgão convocante, para que quórum e membros digam respeito à mesma comunidade de fiéis.
     */
    public boolean aceitaAssociada(ReferenciaOrganizacao convocante, ReferenciaOrganizacao associada) {
        return exigeOrganizacaoAssociada
                && associada.tipo() == TipoOrganizacao.CONSELHO
                && associada.subtipo() == SubtipoConselho.CAEP
                && Objects.equals(convocante.unidadeId(), associada.unidadeId());
    }
}
