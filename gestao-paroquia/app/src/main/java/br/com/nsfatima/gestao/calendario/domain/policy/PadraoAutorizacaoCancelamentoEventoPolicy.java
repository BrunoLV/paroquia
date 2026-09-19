package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de cancelamento de eventos.
 */
public final class PadraoAutorizacaoCancelamentoEventoPolicy implements AutorizacaoCancelamentoEventoPolicy {

    private static final Set<PapelOrganizacional> LEADERSHIP_ROLES = Set.of(
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.COORD_PASTORAL);

    private static final Set<TipoGrupo> LOCAL_SCOPED_ORG_TYPES = Set.of(
            TipoGrupo.PASTORAL);

    @Override
    public ModoRequisicaoCancelamento resolveRequestMode(
            PapelOrganizacional papel,
            TipoGrupo tipoGrupo,
            UUID atorOrgId,
            UUID eventoOrgId) {

        if (isConselhoLeadership(papel, tipoGrupo) || papel == PapelOrganizacional.PAROCO) {
            return ModoRequisicaoCancelamento.IMEDIATO;
        }

        if (papel == PapelOrganizacional.VIGARIO) {
            return ModoRequisicaoCancelamento.REQUER_APROVACAO;
        }

        if (isLocalLeadership(papel, tipoGrupo)) {
            if (atorOrgId == null) {
                throw new OperacaoProibidaException("Usuário não pode cancelar eventos sem um escopo de organização");
            }
            if (eventoOrgId == null || !Objects.equals(eventoOrgId, atorOrgId)) {
                throw new OperacaoProibidaException(
                        "User cannot cancel events outside the responsible organization scope");
            }
            return ModoRequisicaoCancelamento.REQUER_APROVACAO;
        }

        throw new OperacaoProibidaException("Usuário não tem permissão para cancelar eventos");
    }

    @Override
    public void assertCanDecideAprovacao(PapelOrganizacional papel, TipoGrupo tipoGrupo) {
        if (papel == PapelOrganizacional.PAROCO || isConselhoLeadership(papel, tipoGrupo)) {
            return;
        }
        throw new OperacaoProibidaException("Usuário não tem permissão para decidir aprovações de evento");
    }

    private boolean isConselhoLeadership(PapelOrganizacional role, TipoGrupo organizationType) {
        return organizationType == TipoGrupo.CONSELHO && LEADERSHIP_ROLES.contains(role);
    }

    private boolean isLocalLeadership(PapelOrganizacional role, TipoGrupo organizationType) {
        return organizationType != null && LOCAL_SCOPED_ORG_TYPES.contains(organizationType)
                && LEADERSHIP_ROLES.contains(role);
    }
}
