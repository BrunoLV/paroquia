package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

/** Preserva a compatibilidade dos papéis globais clericais com o escopo sintético CLERO. */
public final class CleroCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "CLERO".equalsIgnoreCase(tipoOrganizacao)
                && (papel == PapelOrganizacional.PAROCO || papel == PapelOrganizacional.VIGARIO
                || papel == PapelOrganizacional.PADRE || papel == PapelOrganizacional.DIACONO);
    }
}
