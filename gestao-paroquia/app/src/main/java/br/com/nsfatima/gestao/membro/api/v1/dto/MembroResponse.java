package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MembroResponse(
        UUID id,
        String nomeCompleto,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String endereco,
        UUID usuarioId,
        boolean ativo,
        LocalDate dataBatismo,
        String localBatismo,
        LocalDate dataCrisma,
        LocalDate dataMatrimonio) {
}
