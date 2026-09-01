package br.com.nsfatima.gestao.iam.api.v1.controller;

import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * Expõe o "quem sou eu" da sessão autenticada: identidade, papéis globais (roles) e vínculos organizacionais
 * ativos do usuário logado. É a fonte que o frontend usa para decidir o que renderizar/permitir sem precisar
 * reimplementar as regras de autorização já resolvidas pelo Spring Security no login.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para sessão e perfil do usuário logado")
public class AuthController {

    /**
     * Retorna o perfil do usuário autenticado na requisição atual, incluindo authorities (roles globais e
     * papéis por organização) e a lista de organizações às quais ele possui vínculo ativo.
     * <p>
     * Exemplo: GET /api/v1/auth/me -&gt; 200 OK { "id": "...", "username": "...", "enabled": true,
     * "roles": ["ADMIN"], "organizations": [...] }
     *
     * @param userDetails principal injetado pelo Spring Security; {@code null} quando não há usuário autenticado
     *                     (ex.: endpoint acessado anonimamente), caso em que o método retorna {@code null}
     * @return o perfil completo do usuário logado, ou {@code null} se não houver sessão autenticada
     */
    @GetMapping("/me")
    @Operation(summary = "Obtém o perfil do usuário logado")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return new UsuarioResponse(
                userDetails.getUsuarioId(),
                userDetails.getUsername(),
                userDetails.isEnabled(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()),
                userDetails.getVinculos().stream()
                        .map(this::mapToGrupoParoquialResponse)
                        .collect(Collectors.toList()));
    }

    private GrupoParoquialResponse mapToGrupoParoquialResponse(LeitorVinculoExterno.Vinculo vinculo) {
        return new GrupoParoquialResponse(
                vinculo.organizationId(),
                vinculo.organizationName(),
                TipoGrupo.fromStoredValue(vinculo.organizationType()),
                vinculo.organizationContact(),
                vinculo.organizationCoordinator(),
                vinculo.organizationViceCoordinator(),
                vinculo.organizationActive(),
                null,
                null);
    }
}
