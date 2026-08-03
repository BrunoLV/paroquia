package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.UUID;

/**
 * Representa um membro efetivo de uma organização exibido no organograma ou na listagem de membros —
 * abrangendo tanto vínculos formais quanto o coordenador/vice-coordenador registrados como texto livre.
 */
public record OrganizacaoMembroResponse(
        UUID vinculoId,
        UUID usuarioId,
        String usuarioNome,
        String papel,
        boolean ativo
) {
}
