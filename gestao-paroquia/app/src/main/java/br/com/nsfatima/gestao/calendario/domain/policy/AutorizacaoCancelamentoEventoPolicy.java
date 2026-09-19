package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.util.UUID;

/**
 * Contrato puro de política para regras de autorização de cancelamento de eventos.
 */
public interface AutorizacaoCancelamentoEventoPolicy {

    /**
     * Determina o modo de cancelamento aplicável (imediato ou com aprovação).
     *
     * @param papel papel organizacional do ator
     * @param tipoGrupo tipo do grupo do ator
     * @param atorOrgId ID da organização do ator
     * @param eventoOrgId ID da organização responsável pelo evento
     * @return modo de cancelamento aplicável
     */
    ModoRequisicaoCancelamento resolveRequestMode(
            PapelOrganizacional papel,
            TipoGrupo tipoGrupo,
            UUID atorOrgId,
            UUID eventoOrgId);

    /**
     * Valida se o ator possui permissão para aprovar/recusar solicitações de cancelamento.
     *
     * @param papel papel organizacional do ator
     * @param tipoGrupo tipo do grupo do ator
     */
    void assertCanDecideAprovacao(PapelOrganizacional papel, TipoGrupo tipoGrupo);
}
