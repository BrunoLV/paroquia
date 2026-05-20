package br.com.nsfatima.gestao.iam.api.v1.controller;

import br.com.nsfatima.gestao.iam.api.v1.dto.PasswordResetRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.RolesUpdateRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.UserStatusRequest;
import br.com.nsfatima.gestao.iam.domain.service.UsuarioAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "User Administration", description = "Administrative actions for user management")
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioAdminService;

    public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @Operation(summary = "Enable or disable a user", description = "Global administrators or Organization Coordinators can toggle user status.")
    /**
     * Permite que administradores ou coordenadores gerenciem o acesso ao sistema ativando ou desativando contas conforme necessário.
     * 
     * Exemplo: PATCH /api/v1/usuarios/{id}/status { "enabled": false }
     */
    public void toggleStatus(@PathVariable UUID id, @RequestBody @Valid UserStatusRequest request) {
        usuarioAdminService.toggleActiveStatus(id, request.enabled());
    }

    @PatchMapping("/{id}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user global roles", description = "Only global administrators can update roles.")
    /**
     * Define as permissões globais de um usuário no sistema para controlar seu nível de acesso administrativo em toda a plataforma.
     * 
     * Exemplo: PATCH /api/v1/usuarios/{id}/roles { "roles": ["ADMIN"] }
     */
    public void updateRoles(@PathVariable UUID id, @RequestBody @Valid RolesUpdateRequest request) {
        usuarioAdminService.updateGlobalRoles(id, request.roles());
    }

    @PostMapping("/{id}/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @Operation(summary = "Reset user password", description = "Global administrators or Organization Coordinators can reset passwords.")
    /**
     * Força a atualização da senha de um usuário quando necessário para manter a segurança ou recuperar acessos perdidos.
     * 
     * Exemplo: POST /api/v1/usuarios/{id}/password-reset { "newPassword": "..." }
     */
    public void resetPassword(@PathVariable UUID id, @RequestBody @Valid PasswordResetRequest request) {
        usuarioAdminService.resetPassword(id, request.newPassword());
    }
}
