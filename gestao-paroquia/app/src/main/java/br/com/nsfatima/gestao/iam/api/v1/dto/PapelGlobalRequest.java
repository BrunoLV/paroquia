package br.com.nsfatima.gestao.iam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Payload para conceder ou revogar um papel global (`PAROCO`, `VIGARIO`, `ADMIN`, `SECRETARIA_PAROQUIAL`)
 * a um {@code Fiel} já existente.
 */
public record PapelGlobalRequest(
        @NotNull UUID fielId,
        @NotBlank String papel
) {
}
