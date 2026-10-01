package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import java.util.UUID;

/** Vínculo tardio: exatamente um entre {@code fielId} e {@code clerigoId}, conforme o papel do participante. */
public record VincularParticipanteRequest(UUID fielId, UUID clerigoId) {
}
