package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.UUID;

public record OrganizacaoMembroResponse(
        UUID vinculoId,
        UUID usuarioId,
        String usuarioNome,
        String papel,
        boolean ativo
) {
}
