package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.Set;

/**
 * Implementação pura da política de autorização de patch de eventos, sem frameworks ou I/O.
 */
public final class PadraoEventoPatchAuthorizationPolicy implements EventoPatchAuthorizationPolicy {

    private static final Set<PapelOrganizacional> ORG_EDITOR_ROLES = Set.of(
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

    private static final Set<PapelOrganizacional> ORG_CREATOR_ROLES = Set.of(
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

    private static final Set<PapelOrganizacional> CONSELHO_CRIACAO_IMEDIATA = Set.of(
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL);

    @Override
    public Set<PapelOrganizacional> creatorRoles() {
        return ORG_CREATOR_ROLES;
    }

    @Override
    public Set<PapelOrganizacional> editorRoles() {
        return ORG_EDITOR_ROLES;
    }

    @Override
    public boolean isCreatorRole(PapelOrganizacional papel) {
        return ORG_CREATOR_ROLES.contains(papel);
    }

    @Override
    public boolean isEditorRole(PapelOrganizacional papel) {
        return ORG_EDITOR_ROLES.contains(papel);
    }

    @Override
    public boolean isScopeExempt(PapelOrganizacional papel) {
        return papel == PapelOrganizacional.PAROCO
                || papel == PapelOrganizacional.SECRETARIO
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || papel == PapelOrganizacional.MESA_CPP;
    }

    @Override
    public boolean isLiberacaoConflitoPermitida(PapelOrganizacional papel) {
        return papel == PapelOrganizacional.PAROCO
                || papel == PapelOrganizacional.ADMIN
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || papel == PapelOrganizacional.MESA_CPP;
    }

    @Override
    public ModoRequisicaoCriacao resolveModoRequisicaoCriacao(PapelOrganizacional papel, TipoGrupo tipoGrupo) {
        if (papel == PapelOrganizacional.SECRETARIO) {
            return ModoRequisicaoCriacao.REQUER_APROVACAO;
        }
        if (papel == PapelOrganizacional.PAROCO
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || papel == PapelOrganizacional.MESA_CPP
                || (tipoGrupo == TipoGrupo.CONSELHO && CONSELHO_CRIACAO_IMEDIATA.contains(papel))) {
            return ModoRequisicaoCriacao.IMEDIATO;
        }
        return ModoRequisicaoCriacao.REQUER_APROVACAO;
    }

    @Override
    public void assertCanChangeResponsibleOrganization(PapelOrganizacional papel, TipoGrupo tipoGrupo) {
        boolean conselhoCoordinator = (tipoGrupo == TipoGrupo.CONSELHO && CONSELHO_CRIACAO_IMEDIATA.contains(papel))
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || papel == PapelOrganizacional.MESA_CPP;
        boolean paroco = papel == PapelOrganizacional.PAROCO;

        if (!conselhoCoordinator && !paroco) {
            throw new OperacaoProibidaException(
                    "Apenas coordenador de conselho ou pároco pode alterar a organização responsável");
        }
    }
}
