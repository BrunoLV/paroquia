package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Set;

/** Mantém o catálogo de vínculos válidos para os conselhos paroquiais. */
public final class ConselhoCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    private static final Set<PapelOrganizacional> PAPEIS_PERMITIDOS = Set.of(
            PapelOrganizacional.SECRETARIO_CONSELHO,
            PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL,
            PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO,
            // O coordenador e o vice-coordenador do conselho não deixam de ser membros: o vínculo deles traz o papel,
            // como nas pastorais (o vice assume o papel de secretário de grupo, conforme FuncaoCoordenacao).
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.SECRETARIO_GRUPO);

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "CONSELHO".equalsIgnoreCase(tipoOrganizacao) && PAPEIS_PERMITIDOS.contains(papel);
    }
}
