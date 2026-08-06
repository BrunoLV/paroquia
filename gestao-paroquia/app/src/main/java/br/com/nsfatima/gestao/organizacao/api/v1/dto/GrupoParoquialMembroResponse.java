package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.UUID;

/**
 * Representa um membro efetivo de uma organização exibido no organograma ou na listagem de membros —
 * abrangendo tanto vínculos formais ({@code MembroGrupo}) quanto o coordenador/vice-coordenador registrados
 * como texto livre.
 */
public record GrupoParoquialMembroResponse(
        UUID vinculoId,
        UUID fielId,
        String fielNome,
        String papel,
        boolean ativo
) {
}
