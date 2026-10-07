package br.com.nsfatima.gestao.membro.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload para criar ou atualizar o vínculo de um {@code Fiel} a uma organização (papel de atuação na
 * paróquia). Nunca exige/cria um {@code Usuario} — participação pastoral comum não requer login.
 *
 * @param papel      Papel exercido na organização (ver {@code PapelOrganizacional}); quando corresponder a um
 *                   papel de coordenação, sincroniza automaticamente o campo de liderança na {@code Organizacao}.
 * @param dataEntrada Data de início do vínculo; se omitida na criação, assume a data corrente.
 * @param igrejaId   Igreja/paróquia específica associada ao vínculo, quando aplicável (opcional).
 * @param ativo      Situação do vínculo; se omitido na criação, assume {@code true}.
 */
public record MembroRequest(
        @NotNull(message = "Fiel é obrigatório") UUID fielId,
        @NotNull(message = "Organização é obrigatória") UUID organizacaoId,
        @NotBlank(message = "Papel é obrigatório") String papel,
        LocalDate dataEntrada,
        UUID igrejaId,
        Boolean ativo
) {
}
