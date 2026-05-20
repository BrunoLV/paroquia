package br.com.nsfatima.gestao.iam.api.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para sessão e perfil do usuário logado")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Obtém o perfil do usuário logado")
    /**
     * Identifica o usuário atualmente autenticado na sessão para fornecer dados de perfil e permissões.
     * 
     * Exemplo: GET /api/v1/auth/me -> 200 OK { "id": "...", "username": "...", "enabled": true }
     */
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return new UsuarioResponse(
                userDetails.getUsuarioId(),
                userDetails.getUsername(),
                userDetails.isEnabled());
    }
}
