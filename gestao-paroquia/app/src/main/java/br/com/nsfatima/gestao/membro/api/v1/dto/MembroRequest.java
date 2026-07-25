package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembroRequest(
        @NotNull(message = "Usuário é obrigatório") UUID usuarioId,
        @NotNull(message = "Organização é obrigatória") UUID organizacaoId,
        @NotBlank(message = "Papel é obrigatório") String papel,
        LocalDate dataEntrada,
        UUID igrejaId,
        Boolean ativo
) {
}
