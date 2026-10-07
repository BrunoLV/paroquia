package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Set;

/**
 * Implementação pura da política de autorização de patch de eventos, sem frameworks ou I/O.
 * Na governança canônica, somente o Pároco (ou Bispo Diocesano) cria eventos com efeito imediato,
 * libera conflitos de agenda e altera a organização responsável; coordenadores e secretarias
 * submetem solicitações sujeitas à homologação do Pároco.
 */
public final class PadraoEventoPatchAuthorizationPolicy implements EventoPatchAuthorizationPolicy {

    private static final Set<PapelGlobal> GLOBAL_ACTOR_ROLES = Set.of(
            PapelGlobal.PAROCO,
            PapelGlobal.BISPO_DIOCESANO,
            PapelGlobal.VIGARIO,
            PapelGlobal.DIACONO_PAROQUIAL,
            PapelGlobal.SECRETARIA_PAROQUIAL);

    private static final Set<PapelOrganizacional> ORG_EDITOR_ROLES = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.SECRETARIO_CONSELHO,
            PapelOrganizacional.SECRETARIO_GRUPO);

    private static final Set<PapelOrganizacional> ORG_CREATOR_ROLES = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.SECRETARIO_CONSELHO,
            PapelOrganizacional.SECRETARIO_GRUPO);

    @Override
    public Set<PapelOrganizacional> creatorRoles() {
        return ORG_CREATOR_ROLES;
    }

    @Override
    public Set<PapelOrganizacional> editorRoles() {
        return ORG_EDITOR_ROLES;
    }

    @Override
    public boolean isCreatorRole(String rawRole) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal != null && GLOBAL_ACTOR_ROLES.contains(papelGlobal)) {
            return true;
        }
        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(rawRole);
        return papelOrg != null && ORG_CREATOR_ROLES.contains(papelOrg);
    }

    @Override
    public boolean isEditorRole(String rawRole) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal != null && GLOBAL_ACTOR_ROLES.contains(papelGlobal)) {
            return true;
        }
        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(rawRole);
        return papelOrg != null && ORG_EDITOR_ROLES.contains(papelOrg);
    }

    @Override
    public boolean isScopeExempt(String rawRole) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal == PapelGlobal.PAROCO
                || papelGlobal == PapelGlobal.BISPO_DIOCESANO
                || papelGlobal == PapelGlobal.VIGARIO
                || papelGlobal == PapelGlobal.SECRETARIA_PAROQUIAL) {
            return true;
        }
        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(rawRole);
        return papelOrg == PapelOrganizacional.SECRETARIO_CONSELHO;
    }

    @Override
    public boolean isLiberacaoConflitoPermitida(String rawRole) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        return papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO;
    }

    @Override
    public ModoRequisicaoCriacao resolveModoRequisicaoCriacao(String rawRole, TipoOrganizacao tipoOrganizacao) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO) {
            return ModoRequisicaoCriacao.IMEDIATO;
        }
        return ModoRequisicaoCriacao.REQUER_APROVACAO;
    }

    @Override
    public void assertCanChangeResponsibleOrganization(String rawRole, TipoOrganizacao tipoOrganizacao) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal != PapelGlobal.PAROCO && papelGlobal != PapelGlobal.BISPO_DIOCESANO) {
            throw new OperacaoProibidaException(
                    "Apenas o Pároco pode alterar a organização responsável pelo evento");
        }
    }
}
