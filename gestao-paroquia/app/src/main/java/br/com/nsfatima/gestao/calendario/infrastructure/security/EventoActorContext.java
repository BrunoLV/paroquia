package br.com.nsfatima.gestao.calendario.infrastructure.security;

import java.util.UUID;

public record EventoActorContext(
        String actor,
        String role,
        String organizationType,
        UUID
 organizationId,
        UUID
 usuarioId) {
}
