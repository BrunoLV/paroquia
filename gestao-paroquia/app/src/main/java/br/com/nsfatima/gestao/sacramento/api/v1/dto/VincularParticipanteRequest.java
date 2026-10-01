package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Vínculo tardio feito a partir do registro do sacramento: exatamente um entre {@code fielId} e
 * {@code clerigoId}, mais o tipo de participação que a pessoa teve no sacramento, que deve coincidir com
 * o papel da posição escolhida no termo.
 */
public record VincularParticipanteRequest(UUID fielId, UUID clerigoId, @NotNull PapelParticipante papel) {
}
