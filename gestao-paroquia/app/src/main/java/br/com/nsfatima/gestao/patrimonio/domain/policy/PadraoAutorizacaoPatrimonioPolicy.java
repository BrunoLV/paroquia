package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de patrimônio conforme alçadas canônicas (Cân. 537).
 * Pároco, Bispo Diocesano, Secretaria Paroquial e Membros do Conselho Econômico (CAE) possuem visão global;
 * Coordenadores atuam apenas no escopo da sua unidade territorial.
 */
public final class PadraoAutorizacaoPatrimonioPolicy implements AutorizacaoPatrimonioPolicy {

    private static final Set<PapelGlobal> GLOBAL_EDITOR_ROLES = Set.of(
            PapelGlobal.PAROCO,
            PapelGlobal.BISPO_DIOCESANO,
            PapelGlobal.SECRETARIA_PAROQUIAL);

    private static final Set<PapelOrganizacional> ORG_EDITOR_ROLES = Set.of(
            PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO,
            PapelOrganizacional.COORDENADOR);

    @Override
    public boolean isAtorDeAltoNivel(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return false;
        }
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal == PapelGlobal.PAROCO
                || papelGlobal == PapelGlobal.BISPO_DIOCESANO
                || papelGlobal == PapelGlobal.SECRETARIA_PAROQUIAL) {
            return true;
        }
        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(rawRole);
        return papelOrg == PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO;
    }

    @Override
    public void assertPodeGerirPatrimonio(String rawRole, UUID atorOrganizacaoId, UUID grupoParoquialId) {
        if (!isEditorRole(rawRole)) {
            throw new OperacaoProibidaException("Usuário não tem permissão para gerir ativos patrimoniais");
        }

        if (!isAtorDeAltoNivel(rawRole)
                && atorOrganizacaoId != null
                && !Objects.equals(atorOrganizacaoId, grupoParoquialId)) {
            throw new OperacaoProibidaException("Usuário não pode gerir ativos de outra unidade territorial");
        }
    }

    private boolean isEditorRole(String rawRole) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal != null && GLOBAL_EDITOR_ROLES.contains(papelGlobal)) {
            return true;
        }
        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(rawRole);
        return papelOrg != null && ORG_EDITOR_ROLES.contains(papelOrg);
    }
}
