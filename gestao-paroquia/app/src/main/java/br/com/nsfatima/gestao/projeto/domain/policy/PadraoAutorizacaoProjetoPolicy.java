package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de projetos sem acoplamento a frameworks ou I/O.
 * Na governança canônica, apenas o Pároco (e Bispo Diocesano) detém alçada decisória de alto nível,
 * enquanto coordenadores e secretarias propõem/editam projetos no escopo da sua organização.
 */
public final class PadraoAutorizacaoProjetoPolicy implements AutorizacaoProjetoPolicy {

    private static final Set<PapelGlobal> GLOBAL_EDITOR_ROLES = Set.of(
            PapelGlobal.PAROCO,
            PapelGlobal.BISPO_DIOCESANO,
            PapelGlobal.VIGARIO,
            PapelGlobal.SECRETARIA_PAROQUIAL);

    private static final Set<PapelOrganizacional> ORG_EDITOR_ROLES = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.SECRETARIO_CONSELHO,
            PapelOrganizacional.SECRETARIO_GRUPO);

    @Override
    public boolean isAtorDeAltoNivel(String rawRole, String rawTipoGrupo) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        return papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO;
    }

    @Override
    public void assertCanCreate(
            String rawRole, String rawTipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        if (!isEditorRole(rawRole)) {
            throw new OperacaoProibidaException("Usuário não tem permissão para criar projetos");
        }
        if (!isAtorDeAltoNivel(rawRole, rawTipoGrupo) && atorOrganizacaoId != null
                && !Objects.equals(atorOrganizacaoId, targetOrgId)) {
            throw new OperacaoProibidaException("Usuário não pode criar projetos para outra organização");
        }
    }

    @Override
    public void assertCanEdit(
            String rawRole, String rawTipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        if (!isEditorRole(rawRole)) {
            throw new OperacaoProibidaException("Usuário não tem permissão para editar projetos");
        }
        if (!isAtorDeAltoNivel(rawRole, rawTipoGrupo) && atorOrganizacaoId != null
                && !Objects.equals(atorOrganizacaoId, targetOrgId)) {
            throw new OperacaoProibidaException("Usuário não pode editar projetos de outra organização");
        }
    }

    @Override
    public void assertCanStartOrConclude(
            String rawRole, String rawTipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        if (isAtorDeAltoNivel(rawRole, rawTipoGrupo)) {
            return;
        }
        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(rawRole);
        if (papelOrg == PapelOrganizacional.COORDENADOR
                && atorOrganizacaoId != null
                && Objects.equals(atorOrganizacaoId, targetOrgId)) {
            return;
        }
        throw new OperacaoProibidaException("Usuário não tem permissão para executar esta transição de projeto");
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
