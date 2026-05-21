package br.com.nsfatima.gestao.iam.api.v1.dto;

import java.util.Collection;
import java.util.UUID;

public record UsuarioResponse(
    UUID id,
    String username,
    boolean enabled,
    Collection<String> roles
) {}
