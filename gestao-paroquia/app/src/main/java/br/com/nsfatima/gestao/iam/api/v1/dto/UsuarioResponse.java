package br.com.nsfatima.gestao.iam.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoResponse;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Visão pública de um usuário para autenticação, auditoria e gestão IAM.
 * Lida estritamente com dados de conta/usuário (identidade, estado, credenciais, papéis e organizações),
 * além exclusivamente do nome da pessoa física associada para rotulagem.
 */
public record UsuarioResponse(
        UUID id,
        String username,
        boolean enabled,
        Collection<String> roles,
        Collection<OrganizacaoResponse> organizations,
        String nome,
        UUID fielId,
        UUID clerigoId
) {
    /**
     * Construtor de conveniência sem organizações nem dados de pessoas, usado onde apenas
     * identidade/papéis são relevantes.
     */
    public UsuarioResponse(UUID id, String username, boolean enabled, Collection<String> roles) {
        this(id, username, enabled, roles, List.of(), null, null, null);
    }

    /**
     * Construtor de conveniência com organizações vinculadas.
     */
    public UsuarioResponse(UUID id,
                           String username,
                           boolean enabled,
                           Collection<String> roles,
                           Collection<OrganizacaoResponse> organizations) {
        this(id, username, enabled, roles, organizations, null, null, null);
    }
}
