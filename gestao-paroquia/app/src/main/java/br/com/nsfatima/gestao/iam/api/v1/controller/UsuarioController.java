package br.com.nsfatima.gestao.iam.api.v1.controller;

import br.com.nsfatima.gestao.iam.api.v1.dto.AtualizarUsuarioRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.CriarUsuarioRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.application.usecase.AprovarUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.CriarUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.ObterUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.ListarUsuariosUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.AtualizarUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.service.UsuarioAuthorizationService;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.AdicionarVinculoRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.VinculoResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListarVinculosUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.RemoverVinculoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de usuários e gestão dos seus vínculos organizacionais (membros). Todo cadastro é administrativo
 * (via {@link CriarUsuarioUseCase}, exige um {@code Fiel} já existente) — não há mais autocadastro público;
 * {@link AprovarUsuarioUseCase} segue existindo para o fluxo de aprovação de usuários pendentes criados
 * administrativamente. A autorização por endpoint combina {@code @PreAuthorize} de papel global com
 * checagens finas de escopo organizacional feitas por {@link UsuarioAuthorizationService} (admin ou
 * coordenador da organização do usuário-alvo).
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários e permissões (IAM)")
public class UsuarioController {

    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final ObterUsuarioUseCase obterUsuarioUseCase;
    private final CriarUsuarioUseCase criarUsuarioUseCase;
    private final AtualizarUsuarioUseCase atualizarUsuarioUseCase;
    private final AdicionarVinculoUseCase adicionarVinculoUseCase;
    private final RemoverVinculoUseCase removerVinculoUseCase;
    private final ListarVinculosUseCase listarVinculosUseCase;
    private final UsuarioAuthorizationService authorizationService;
    private final AprovarUsuarioUseCase aprovarUsuarioUseCase;

    public UsuarioController(
            ListarUsuariosUseCase listarUsuariosUseCase,
            ObterUsuarioUseCase obterUsuarioUseCase,
            CriarUsuarioUseCase criarUsuarioUseCase,
            AtualizarUsuarioUseCase atualizarUsuarioUseCase,
            AdicionarVinculoUseCase adicionarVinculoUseCase,
            RemoverVinculoUseCase removerVinculoUseCase,
            ListarVinculosUseCase listarVinculosUseCase,
            UsuarioAuthorizationService authorizationService,
            AprovarUsuarioUseCase aprovarUsuarioUseCase) {
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.obterUsuarioUseCase = obterUsuarioUseCase;
        this.criarUsuarioUseCase = criarUsuarioUseCase;
        this.atualizarUsuarioUseCase = atualizarUsuarioUseCase;
        this.adicionarVinculoUseCase = adicionarVinculoUseCase;
        this.removerVinculoUseCase = removerVinculoUseCase;
        this.listarVinculosUseCase = listarVinculosUseCase;
        this.authorizationService = authorizationService;
        this.aprovarUsuarioUseCase = aprovarUsuarioUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'VIGARIO', 'SECRETARIA_PAROQUIAL', 'COORDENADOR', 'SECRETARIO_CONSELHO')")
    @Operation(summary = "Lista todos os usuários")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários recuperada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<UsuarioResponse> listar() {
        return listarUsuariosUseCase.execute();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GLOBAL_ADMIN', 'PAROCO', 'GLOBAL_PAROCO', 'SECRETARIA_PAROQUIAL', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Dados cadastrais inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Conflito de username ou já vinculado")
    })
    public UUID criar(@RequestBody @Valid CriarUsuarioRequest request, Authentication authentication) {
        boolean isSecretaria = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.contains("SECRETARIA_PAROQUIAL"));
        boolean isAdminOrParoco = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.contains("ADMIN") || a.contains("PAROCO") || a.contains("BISPO_DIOCESANO"));
        boolean pendenteAprovacao = isSecretaria && !isAdminOrParoco;

        return criarUsuarioUseCase.execute(
                request.username(),
                request.password(),
                request.fielId(),
                request.clerigoId(),
                pendenteAprovacao);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL', 'COORDENADOR')")
    @Operation(summary = "Obtém detalhes de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = UsuarioResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public UsuarioResponse obter(
            @Parameter(description = "Identificador único do usuário") @PathVariable UUID id) {
        authorizationService.requireAdminOrCoordinatorOf(id);
        return obterUsuarioUseCase.execute(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GLOBAL_ADMIN', 'PAROCO', 'GLOBAL_PAROCO')")
    @Operation(summary = "Atualiza um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public void atualizar(
            @Parameter(description = "Identificador único do usuário") @PathVariable UUID id,
            @RequestBody @Valid AtualizarUsuarioRequest request) {
        atualizarUsuarioUseCase.execute(id, request.username(), request.enabled());
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GLOBAL_ADMIN', 'PAROCO', 'GLOBAL_PAROCO', 'BISPO_DIOCESANO', 'GLOBAL_BISPO_DIOCESANO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Aprova um novo usuário e ativa suas solicitações de pastorais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário aprovado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public void aprovar(
            @Parameter(description = "Identificador único do usuário") @PathVariable UUID id) {
        aprovarUsuarioUseCase.execute(id);
    }

    @GetMapping("/{id}/membros")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL', 'COORDENADOR')")
    @Operation(summary = "Lista associações de um usuário a organizações")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vínculos recuperados com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VinculoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public List<VinculoResponse> listarVinculos(
            @Parameter(description = "Identificador único do usuário") @PathVariable UUID id) {
        authorizationService.requireAdminOrCoordinatorOf(id);
        return listarVinculosUseCase.execute(id);
    }

    @PostMapping("/{id}/membros")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL', 'COORDENADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona associação a organização")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vínculo associado com sucesso",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Dados do vínculo inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário ou organização não encontrada")
    })
    public UUID adicionarVinculo(
            @Parameter(description = "Identificador único do usuário") @PathVariable UUID id,
            @RequestBody @Valid AdicionarVinculoRequest request) {
        authorizationService.requireAdminOrCoordinatorOfOrganization(request.grupoParoquialId());
        return adicionarVinculoUseCase.execute(id,
                request.grupoParoquialId(), request.tipo(), request.papel(), request.igrejaVinculoId());
    }

    @DeleteMapping("/{id}/membros/{vinculoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL', 'COORDENADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove associação a organização")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vínculo removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário ou vínculo não encontrado")
    })
    public void removerVinculo(
            @Parameter(description = "Identificador único do usuário") @PathVariable UUID id,
            @Parameter(description = "Identificador único do vínculo") @PathVariable UUID vinculoId) {
        authorizationService.requireAdminOrCoordinatorOf(id);
        removerVinculoUseCase.execute(vinculoId);
    }
}
