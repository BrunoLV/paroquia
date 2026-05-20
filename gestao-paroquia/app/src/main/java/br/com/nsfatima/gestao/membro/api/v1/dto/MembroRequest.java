package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MembroRequest(
        @NotBlank @Size(max = 255) String nomeCompleto,
        LocalDate dataNascimento,
        @Size(max = 255) String email,
        @Size(max = 20) String telefone,
        @Size(max = 500) String endereco,
        UUID usuarioId,
        LocalDate dataBatismo,
        String localBatismo,
        LocalDate dataCrisma,
        LocalDate dataMatrimonio) {
}
