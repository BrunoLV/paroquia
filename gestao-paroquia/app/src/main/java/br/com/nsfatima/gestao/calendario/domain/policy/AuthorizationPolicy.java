package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.stereotype.Component;

/**
 * Política que valida se um papel organizacional é compatível com o tipo de
 * organização à qual o usuário está vinculado — regra base de autorização
 * eclesial usada antes de checagens específicas de ação (ver
 * {@code EventoPatchAuthorizationService}, {@code EventoCancelamentoAuthorizationService}).
 * Cada tipo de organização (pastoral, movimento, matriz/comunidade, clero, conselho)
 * só aceita um subconjunto fechado de papéis coerentes com sua estrutura.
 */
@Component
public class AuthorizationPolicy {

    /**
     * Verifica se o papel organizacional informado é um papel válido para o tipo de
     * organização informado, segundo a hierarquia eclesial da paróquia.
     *
     * @param organizationType código do tipo de organização (ex.: pastoral, matriz, clero)
     * @param role código do papel organizacional do usuário
     * @return {@code true} se o papel é compatível com o tipo de organização; {@code false} se algum dos dois não for
     *     reconhecido ou não for compatível
     */
    public boolean isRoleAllowed(String organizationType, String role) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(role);

        // "CLERO" é apenas um sentinel informativo de escopo global desde a track
        // linguagem_ubiqua_dominio (não é mais um TipoGrupo válido — Clero virou UnidadeTerritorial).
        // PAROCO/VIGARIO são papéis globais (track roles_globais_vs_escopo), mas continuam sendo
        // representados sob esse sentinel em contextos legados/sintéticos (ex.: EventoActorContextResolver).
        if ("CLERO".equalsIgnoreCase(organizationType)) {
            return normalizedRole == PapelOrganizacional.PAROCO
                    || normalizedRole == PapelOrganizacional.VIGARIO
                    || normalizedRole == PapelOrganizacional.PADRE
                    || normalizedRole == PapelOrganizacional.DIACONO;
        }

        TipoGrupo normalizedOrg = TipoGrupo.fromStoredValue(organizationType);

        if (normalizedOrg == null || normalizedRole == null) {
            return false;
        }

        return switch (normalizedOrg) {
            case PASTORAL -> normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                    || normalizedRole == PapelOrganizacional.COORD_PASTORAL_LOCAL
                    || normalizedRole == PapelOrganizacional.COORD_PASTORAL
                    || normalizedRole == PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                    || normalizedRole == PapelOrganizacional.MEMBRO_PASTORAL;
            case MOVIMENTO -> normalizedRole == PapelOrganizacional.COORD_MOVIMENTO
                    || normalizedRole == PapelOrganizacional.MEMBRO_MOVIMENTO;
            // CONSELHO colapsa os antigos CPP/CPC/CONSELHO_PASTORAL/CONSELHO_ECONOMICO (track
            // linguagem_ubiqua_dominio) — PAROCO/VIGARIO não aparecem mais aqui: viraram papéis globais
            // (track roles_globais_vs_escopo), não vínculos de MembroGrupo.
            case CONSELHO -> normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                    || normalizedRole == PapelOrganizacional.MEMBRO_CPC
                    || normalizedRole == PapelOrganizacional.MEMBRO_CPP
                    || normalizedRole == PapelOrganizacional.MESA_CPP
                    || normalizedRole == PapelOrganizacional.MEMBRO_CAE
                    || normalizedRole == PapelOrganizacional.SECRETARIO
                    || normalizedRole == PapelOrganizacional.COORD_PASTORAL_LOCAL
                    || normalizedRole == PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL;
            case MINISTERIO -> false;
        };
    }
}
