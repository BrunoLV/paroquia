package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Set;

/** Mantém o catálogo de vínculos válidos para uma pastoral. */
public final class PastoralCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    private static final Set<PapelOrganizacional> PAPEIS_PERMITIDOS = Set.of(
            PapelOrganizacional.COORD_PASTORAL_GLOBAL, PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
            PapelOrganizacional.MEMBRO_PASTORAL);

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "PASTORAL".equalsIgnoreCase(tipoOrganizacao) && PAPEIS_PERMITIDOS.contains(papel);
    }
}
