package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.List;

public record OrganogramaNodeResponse(
        String id,
        String label,
        String type,          // e.g. "ORGANIZACAO", "MEMBRO", "GRUPO"
        String subtype,       // e.g. "CLERO", "CONSELHO_PASTORAL", "CONSELHO_ECONOMICO",
        // "PASTORAL", "MOVIMENTO", "MEMBER_ROLE"
        String responsavelNome,
        String responsavelPapel,
        String viceResponsavelNome,
        List<OrganogramaNodeResponse> children
) {
}
