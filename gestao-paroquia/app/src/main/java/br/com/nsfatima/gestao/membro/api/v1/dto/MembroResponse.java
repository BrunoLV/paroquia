package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MembroResponse(
        UUID id,
        UUID usuarioId,
        String usuarioNome,
        String usuarioEmail,
        String usuarioTelefone,
        UUID organizacaoId,
        String organizacaoNome,
        String papel,
        LocalDate dataEntrada,
        boolean ativo,
        UUID igrejaId
) {
}
