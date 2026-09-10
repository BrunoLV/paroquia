package br.com.nsfatima.gestao.governanca.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.governanca.domain.exception.AnoParoquialNaoAutorizadoException;

/** Autoriza pároco, administrador e coordenação canônica do conselho para a trava anual. */
public final class ConselhoOuParocoAutorizacaoAnoParoquialPolicy implements AutorizacaoAnoParoquialPolicy {

    @Override
    public void assertPodeGerir(PapelOrganizacional papel, TipoGrupo tipoGrupo) {
        boolean autorizado = papel == PapelOrganizacional.PAROCO || papel == PapelOrganizacional.ADMIN
                || tipoGrupo == TipoGrupo.CONSELHO && (papel == PapelOrganizacional.PRESIDENTE_CPC
                || papel == PapelOrganizacional.MESA_CPP || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL);
        if (!autorizado) {
            throw new AnoParoquialNaoAutorizadoException(
                    "Apenas o Paroco ou o Coordenador do Conselho podem gerir o Ano Paroquial");
        }
    }
}
