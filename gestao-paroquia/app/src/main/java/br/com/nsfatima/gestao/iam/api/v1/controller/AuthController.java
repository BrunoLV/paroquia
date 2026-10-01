package br.com.nsfatima.gestao.iam.api.v1.controller;

import br.com.nsfatima.gestao.iam.api.v1.dto.AlterarMinhaSenhaRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.SolicitarResetSenhaRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.application.usecase.AlterarSenhaUseCase;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Expõe o "quem sou eu" da sessão autenticada: identidade, papéis globais (roles) e vínculos organizacionais
 * ativos do usuário logado, além de gerenciar os fluxos de autoatendimento de senha (troca de senha por usuário
 * autenticado e solicitação pública de redefinição de acesso).
 */
@RestController
@RequestMapping("/api/v1/autenticacao")
@Tag(name = "Autenticação", description = "Endpoints para sessão, perfil e gestão de credenciais")
public class AuthController {

    private static final String TARGET_USUARIO = "usuario";
    private static final String SUCESSO = "sucesso";

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlterarSenhaUseCase alterarSenhaUseCase;
    private final AuditoriaPersistenceService auditLogService;

    public AuthController(
            UsuarioJpaRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AlterarSenhaUseCase alterarSenhaUseCase,
            AuditoriaPersistenceService auditLogService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.alterarSenhaUseCase = alterarSenhaUseCase;
        this.auditLogService = auditLogService;
    }

    /**
     * Retorna o perfil do usuário autenticado na requisição atual, incluindo authorities e lista de vínculos ativos.
     *
     * @param userDetails principal injetado pelo Spring Security
     * @return o perfil completo do usuário logado, ou null se não houver sessão autenticada
     */
    @GetMapping("/meu-perfil")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Obtém o perfil do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil do usuário ou null se anônimo",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class)))
    })
    public UsuarioResponse meuPerfil(@AuthenticationPrincipal UsuarioDetails userDetails) {
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

    /**
     * Altera a senha do próprio usuário logado, exigindo a confirmação da senha atual.
     *
     * @param userDetails principal autenticado
     * @param request payload com senha atual e nova senha
     */
    @PostMapping("/alterar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Altera a senha do usuário autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha atual incorreta"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public void alterarSenha(
            @AuthenticationPrincipal UsuarioDetails userDetails,
            @RequestBody @Valid AlterarMinhaSenhaRequest request) {
        if (userDetails == null) {
            throw new AccessDeniedException("Não autenticado");
        }
        UsuarioEntity usuario = usuarioRepository.findById(userDetails.getUsuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(userDetails.getUsuarioId()));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("A senha atual informada está incorreta");
        }

        alterarSenhaUseCase.execute(usuario.getId(), request.novaSenha());
        auditLogService.log(TARGET_USUARIO, "alterar-propria-senha", TARGET_USUARIO, SUCESSO, Map.of(
                "usuarioId", usuario.getId(),
                "action", "ALTERAR_SENHA"
        ));
    }

    /**
     * Registra solicitação pública de redefinição de senha para conta ativa.
     * Resposta genérica 204 para prevenir enumeração de contas por agentes maliciosos externos.
     *
     * @param request payload com o username a ser resetado
     */
    @PostMapping("/solicitar-reset-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Solicita a redefinição de acesso/senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitação recebida com sucesso")
    })
    public void solicitarResetSenha(@RequestBody @Valid SolicitarResetSenhaRequest request) {
        usuarioRepository.findByUsernameIgnoreCase(request.username())
                .filter(UsuarioEntity::isEnabled)
                .ifPresent(u -> auditLogService.log("anonimo", "solicitar-reset-senha", TARGET_USUARIO, SUCESSO, Map.of(
                        "usuarioId", u.getId(),
                        "action", "SOLICITAR_RESET_SENHA"
                )));
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
