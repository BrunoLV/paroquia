package br.com.nsfatima.gestao.iam.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoResponse;
import java.util.List;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

public record UsuarioResponse(
    UUID id,
    String username,
    boolean enabled,
    Collection<String> roles,
    Collection<OrganizacaoResponse> organizations,
    String nome,
    String telefone,
    LocalDate dataNascimento,
    UUID igrejaId,
    String igrejaNome,
    boolean batizado,
    boolean primeiraComunhao,
    boolean crismado,
    boolean casadoIgrejaCatolica
) {
    public UsuarioResponse(UUID id, String username, boolean enabled, Collection<String> roles) {
        this(id, username, enabled, roles, List.of(), null, null, null, null, null, false, false, false, false);
    }

    public UsuarioResponse(UUID id, String username, boolean enabled, Collection<String> roles, Collection<OrganizacaoResponse> organizations) {
        this(id, username, enabled, roles, organizations, null, null, null, null, null, false, false, false, false);
    }
}

