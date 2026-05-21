package br.com.nsfatima.gestao.iam.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoResponse;
import java.util.List;
import java.util.Collection;
import java.util.UUID;

public record UsuarioResponse(
    UUID id,
    String username,
    boolean enabled,
    Collection<String> roles,
    Collection<OrganizacaoResponse> organizations
) {
    public UsuarioResponse(UUID id, String username, boolean enabled, Collection<String> roles) {
        this(id, username, enabled, roles, List.of());
    }
}
