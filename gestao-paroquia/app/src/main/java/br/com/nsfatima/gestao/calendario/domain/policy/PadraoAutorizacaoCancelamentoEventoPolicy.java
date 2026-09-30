package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de cancelamento de eventos.
 * Segue o princípio canônico de que apenas o Pároco cancela imediatamente e homologa
 * aprovações de cancelamento, enquanto coordenações e secretarias solicitam no seu escopo.
 */
public final class PadraoAutorizacaoCancelamentoEventoPolicy implements AutorizacaoCancelamentoEventoPolicy {

    @Override
    public ModoRequisicaoCancelamento resolveRequestMode(
            String rawRole,
            String rawTipoGrupo,
            UUID atorOrgId,
            UUID eventoOrgId) {

        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO) {
            return ModoRequisicaoCancelamento.IMEDIATO;
        }

        if (papelGlobal == PapelGlobal.VIGARIO || papelGlobal == PapelGlobal.SECRETARIA_PAROQUIAL) {
            return ModoRequisicaoCancelamento.REQUER_APROVACAO;
        }

        PapelOrganizacional papelOrg = PapelOrganizacional.fromStoredValue(rawRole);
        if (papelOrg == PapelOrganizacional.SECRETARIO_CONSELHO) {
            return ModoRequisicaoCancelamento.REQUER_APROVACAO;
        }

        if (papelOrg == PapelOrganizacional.COORDENADOR || papelOrg == PapelOrganizacional.SECRETARIO_GRUPO) {
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
    public void assertCanDecideAprovacao(String rawRole, String rawTipoGrupo) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        if (papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO) {
            return;
        }
        throw new OperacaoProibidaException("Apenas o Pároco possui autoridade canônica para decidir aprovações de cancelamento");
    }
}
