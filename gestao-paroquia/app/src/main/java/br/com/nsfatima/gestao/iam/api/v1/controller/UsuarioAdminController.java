package br.com.nsfatima.gestao.iam.api.v1.controller;

import br.com.nsfatima.gestao.iam.api.v1.dto.RedefinirSenhaRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.AtualizarPapeisRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioStatusRequest;
import br.com.nsfatima.gestao.iam.application.service.UsuarioAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Ações administrativas sensíveis sobre contas de usuário (ativar/desativar, redefinir senha, alterar papéis
 * globais). Todas as operações são delegadas a {@link UsuarioAdminService}, que reaplica a checagem de
 * autorização (admin global ou coordenador da organização do usuário-alvo) independentemente do
 * {@code @PreAuthorize} do endpoint, e registra auditoria de cada ação.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Administração de Usuários", description = "Ações administrativas para gestão de usuários")
public class UsuarioAdminController {

    private final UsuarioAdminService usuarioAdminService;

    public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    /**
     * Ativa ou desativa o acesso de um usuário ao sistema. Usuário desativado não consegue autenticar,
     * mesmo com credenciais válidas — é o mecanismo usado para bloquear acesso sem excluir a conta (histórico
     * e vínculos são preservados).
     * <p>
     * Exemplo: PATCH /api/v1/usuarios/{id}/status { "enabled": false }
     *
     * @param id      identificador do usuário alvo
     * @param request novo estado desejado ({@code enabled})
     * @throws org.springframework.security.access.AccessDeniedException se o ator não for admin global nem
     *     coordenador de uma organização à qual o usuário-alvo pertence
     */
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'GLOBAL_ADMIN', 'PAROCO', 'GLOBAL_PAROCO')")
    @Operation(summary = "Ativa ou desativa um usuário", description = "Apenas administradores globais ou pároco podem alternar o status do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status do usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public void alternarStatus(
            @Parameter(description = "Identificador único do usuário", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid UsuarioStatusRequest request) {
        usuarioAdminService.alternarStatusAtivo(id, request.enabled());
    }

    /**
     * Substitui integralmente os papéis (roles) globais de um usuário, definindo seu nível de acesso
     * administrativo em toda a plataforma (ex.: {@code ROLE_ADMIN}). Diferente de vínculos organizacionais
     * (escopados a uma organização), papéis globais valem no sistema inteiro — por isso a operação é
     * restrita a administradores globais, sem exceção para coordenadores.
     * <p>
     * Exemplo: PATCH /api/v1/usuarios/{id}/papeis { "roles": ["ADMIN"] }
     *
     * @param id      identificador do usuário alvo
     * @param request lista de papéis que substitui os papéis globais atuais do usuário
     */
    @PatchMapping("/{id}/papeis")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'GLOBAL_ADMIN')")
    @Operation(summary = "Atualiza papéis globais do usuário", description = "Apenas administradores globais podem atualizar papéis.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Papéis globais atualizados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public void atualizarPapeis(
            @Parameter(description = "Identificador único do usuário", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarPapeisRequest request) {
        usuarioAdminService.atualizarPapeisGlobais(id, request.roles());
    }

    /**
     * Redefine a senha de um usuário de forma administrativa (sem exigir a senha atual), usada quando o
     * usuário perdeu acesso à conta ou por política de segurança. A nova senha é sempre recebida em texto
     * puro e persistida apenas como hash.
     * <p>
     * Exemplo: POST /api/v1/usuarios/{id}/redefinir-senha { "newPassword": "..." }
     *
     * @param id      identificador do usuário alvo
     * @param request nova senha em texto puro, a ser criptografada antes da persistência
     * @throws org.springframework.security.access.AccessDeniedException se o ator não for admin global nem
     *     coordenador de uma organização à qual o usuário-alvo pertence
     */
    @PostMapping("/{id}/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'GLOBAL_ADMIN', 'PAROCO', 'GLOBAL_PAROCO', 'COORDENADOR')")
    @Operation(summary = "Redefine a senha de um usuário", description = "Administradores globais, pároco ou coordenadores" +
            " de organização podem redefinir senhas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public void redefinirSenha(
            @Parameter(description = "Identificador único do usuário", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid RedefinirSenhaRequest request) {
        usuarioAdminService.redefinirSenha(id, request.newPassword());
    }
}
