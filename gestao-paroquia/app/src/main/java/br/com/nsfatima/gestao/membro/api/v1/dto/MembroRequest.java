package br.com.nsfatima.gestao.membro.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload para criar ou atualizar o vínculo de um usuário a uma organização (papel de atuação na paróquia).
 *
 * @param papel      Papel exercido na organização (ex.: {@code COORDENADOR}, {@code VICE_COORDENADOR}); quando
 *                   informado como {@code COORDENADOR} ou {@code VICE_COORDENADOR}, sincroniza automaticamente o
 *                   respectivo campo de liderança na {@code Organizacao}.
 * @param dataEntrada Data de início do vínculo; se omitida na criação, assume a data corrente.
 * @param igrejaId   Igreja/paróquia específica associada ao vínculo, quando aplicável (opcional).
 * @param ativo      Situação do vínculo; se omitido na criação, assume {@code true}.
 */
public record MembroRequest(
        @NotNull(message = "Usuário é obrigatório") UUID usuarioId,
        @NotNull(message = "Organização é obrigatória") UUID organizacaoId,
        @NotBlank(message = "Papel é obrigatório") String papel,
        LocalDate dataEntrada,
        UUID igrejaId,
        Boolean ativo
) {
}
