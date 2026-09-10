package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

/** Mantém o catálogo de vínculos válidos para um movimento. */
public final class MovimentoCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "MOVIMENTO".equalsIgnoreCase(tipoOrganizacao)
                && (papel == PapelOrganizacional.COORD_MOVIMENTO || papel == PapelOrganizacional.MEMBRO_MOVIMENTO);
    }
}
