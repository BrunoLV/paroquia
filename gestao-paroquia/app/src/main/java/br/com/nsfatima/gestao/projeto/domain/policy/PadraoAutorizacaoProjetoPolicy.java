package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de projetos sem acoplamento a frameworks ou I/O.
 */
public final class PadraoAutorizacaoProjetoPolicy implements AutorizacaoProjetoPolicy {

    private static final Set<PapelOrganizacional> PROJECT_EDITOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.COORD_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.SECRETARIO,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.VIGARIO,
            PapelOrganizacional.PADRE);

    @Override
    public boolean isAtorDeAltoNivel(PapelOrganizacional papel, TipoGrupo tipoGrupo) {
        if (papel == null) {
            return false;
        }
        return papel == PapelOrganizacional.PAROCO
                || papel == PapelOrganizacional.ADMIN
                || (tipoGrupo == TipoGrupo.CONSELHO
                && (papel == PapelOrganizacional.PRESIDENTE_CPC
                || papel == PapelOrganizacional.MESA_CPP
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL));
    }

    @Override
    public void assertCanCreate(
            PapelOrganizacional papel, TipoGrupo tipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        if (!PROJECT_EDITOR_ROLES.contains(papel)) {
            throw new ForbiddenOperationException("Usuário não tem permissão para criar projetos");
        }
        if (!isAtorDeAltoNivel(papel, tipoGrupo) && atorOrganizacaoId != null
                && !Objects.equals(atorOrganizacaoId, targetOrgId)) {
            throw new ForbiddenOperationException("Usuário não pode criar projetos para outra organização");
        }
    }

    @Override
    public void assertCanEdit(
            PapelOrganizacional papel, TipoGrupo tipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        if (!PROJECT_EDITOR_ROLES.contains(papel)) {
            throw new ForbiddenOperationException("Usuário não tem permissão para editar projetos");
        }
        if (!isAtorDeAltoNivel(papel, tipoGrupo) && atorOrganizacaoId != null
                && !Objects.equals(atorOrganizacaoId, targetOrgId)) {
            throw new ForbiddenOperationException("Usuário não pode editar projetos de outra organização");
        }
    }

    @Override
    public void assertCanStartOrConclude(
            PapelOrganizacional papel, TipoGrupo tipoGrupo, UUID atorOrganizacaoId, UUID targetOrgId) {
        if (isAtorDeAltoNivel(papel, tipoGrupo)) {
            return;
        }
        boolean isOrgLeader = papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || papel == PapelOrganizacional.COORD_PASTORAL_LOCAL
                || papel == PapelOrganizacional.COORD_MOVIMENTO
                || papel == PapelOrganizacional.COORD_PASTORAL
                || papel == PapelOrganizacional.PRESIDENTE_CPC;

        if (isOrgLeader && atorOrganizacaoId != null && Objects.equals(atorOrganizacaoId, targetOrgId)) {
            return;
        }
        throw new ForbiddenOperationException("Usuário não tem permissão para executar esta transição de projeto");
    }
}
