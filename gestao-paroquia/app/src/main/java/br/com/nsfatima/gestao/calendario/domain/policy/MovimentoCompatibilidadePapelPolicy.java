package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Set;

/** Mantém o catálogo de vínculos válidos para um movimento. */
public final class MovimentoCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    private static final Set<PapelOrganizacional> PAPEIS_PERMITIDOS = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.SECRETARIO_GRUPO,
            PapelOrganizacional.MEMBRO_ATIVO,
            PapelOrganizacional.VOLUNTARIO);

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "MOVIMENTO".equalsIgnoreCase(tipoOrganizacao) && PAPEIS_PERMITIDOS.contains(papel);
    }
}
