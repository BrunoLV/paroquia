package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Set;

/** Mantém o catálogo de vínculos válidos para os conselhos paroquiais. */
public final class ConselhoCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    private static final Set<PapelOrganizacional> PAPEIS_PERMITIDOS = Set.of(
            PapelOrganizacional.PRESIDENTE_CPC, PapelOrganizacional.MEMBRO_CPC,
            PapelOrganizacional.MEMBRO_CPP, PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.MEMBRO_CAE, PapelOrganizacional.SECRETARIO,
            PapelOrganizacional.COORD_PASTORAL_LOCAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL);

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "CONSELHO".equalsIgnoreCase(tipoOrganizacao) && PAPEIS_PERMITIDOS.contains(papel);
    }
}
