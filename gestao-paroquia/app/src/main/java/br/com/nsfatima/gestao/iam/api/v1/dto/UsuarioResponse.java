package br.com.nsfatima.gestao.iam.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialResponse;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Visão pública de um usuário: identidade, estado de habilitação, papéis globais ({@code roles}) e
 * organizações às quais possui vínculo ativo ({@code organizations}), além dos dados cadastrais pastorais
 * (nome, telefone, data de nascimento e situação sacramental). Os campos criptografados na entidade
 * ({@code nome}, {@code telefone}, {@code dataNascimento}) já chegam decifrados aqui.
 */
public record UsuarioResponse(
        UUID id,
        String username,
        boolean enabled,
        Collection<String> roles,
        Collection<GrupoParoquialResponse> organizations,
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
    /**
     * Construtor de conveniência sem organizações nem dados cadastrais pastorais, usado onde apenas
     * identidade/papéis são relevantes.
     */
    public UsuarioResponse(UUID id, String username, boolean enabled, Collection<String> roles) {
        this(id, username, enabled, roles, List.of(), null, null, null, null, null, false, false, false, false);
    }

    /**
     * Construtor de conveniência sem dados cadastrais pastorais, usado onde identidade, papéis e
     * organizações vinculadas bastam.
     */
    public UsuarioResponse(UUID id,
                           String username, boolean enabled, Collection<String> roles,
                           Collection<GrupoParoquialResponse> organizations) {

        this(id, username, enabled, roles, organizations, null, null, null, null, null, false, false, false, false);
    }
}

