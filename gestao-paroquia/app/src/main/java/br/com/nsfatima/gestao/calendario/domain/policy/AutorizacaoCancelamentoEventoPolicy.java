package br.com.nsfatima.gestao.calendario.domain.policy;

import java.util.UUID;

/**
 * Contrato puro de política para regras de autorização de cancelamento de eventos.
 */
public interface AutorizacaoCancelamentoEventoPolicy {

    /**
     * Determina o modo de cancelamento aplicável (imediato ou com aprovação).
     *
     * @param rawRole papel global ou organizacional do ator
     * @param rawTipoOrganizacao tipo do grupo do ator
     * @param atorOrgId ID da organização do ator
     * @param eventoOrgId ID da organização responsável pelo evento
     * @return modo de cancelamento aplicável
     */
    ModoRequisicaoCancelamento resolveRequestMode(
            String rawRole,
            String rawTipoOrganizacao,
            UUID atorOrgId,
            UUID eventoOrgId);

    /**
     * Valida se o ator possui permissão para aprovar/recusar solicitações de cancelamento.
     *
     * @param rawRole papel global ou organizacional do ator
     * @param rawTipoOrganizacao tipo do grupo do ator
     */
    void assertCanDecideAprovacao(String rawRole, String rawTipoOrganizacao);
}
