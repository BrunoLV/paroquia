package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.List;

/**
 * Nó recursivo da árvore de organograma retornada por {@code GetOrganogramaUseCase}. Um nó pode representar
 * tanto uma organização ({@code type = "GRUPO_PAROQUIAL"}, com {@code subtype} igual ao {@code TipoGrupo})
 * quanto um membro individual ({@code type = "MEMBRO"}, folha sem filhos); membros são adicionados como
 * filhos do nó da organização a que pertencem, junto com os nós de organizações filhas.
 */
public record OrganogramaNodeResponse(
        String id,
        String label,
        String type,          // e.g. "GRUPO_PAROQUIAL", "MEMBRO", "GRUPO"
        String subtype,       // e.g. "CLERO", "CONSELHO_PASTORAL", "CONSELHO_ECONOMICO",
        // "PASTORAL", "MOVIMENTO", "MEMBER_ROLE"
        String responsavelNome,
        String responsavelPapel,
        String viceResponsavelNome,
        List<OrganogramaNodeResponse> children
) {
}
