package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.DecidirAjusteCadastralRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.PortabilidadeTitularResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.PreferenciasPrivacidadeRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.SolicitacaoAjusteCadastralResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.SolicitarAjusteCadastralRequest;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarAjusteCadastralUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.ObterFielUseCase;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Fila de moderação para alterações solicitadas pelo próprio fiel. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ajustes Cadastrais de Fiéis", description = "Endpoints para solicitação e moderação de ajustes cadastrais")
public class AjusteCadastralController {

    private static final String MSG_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado.";

    private final GerenciarAjusteCadastralUseCase useCase;
    private final EventoActorContextResolver ator;
    private final UsuarioJpaRepository usuarios;
    private final ObterFielUseCase getFiel;
    private final FielJpaRepository fielRepository;

    @Autowired
    public AjusteCadastralController(
            GerenciarAjusteCadastralUseCase useCase,
            EventoActorContextResolver ator,
            UsuarioJpaRepository usuarios,
            ObterFielUseCase getFiel,
            FielJpaRepository fielRepository) {
        this.useCase = useCase;
        this.ator = ator;
        this.usuarios = usuarios;
        this.getFiel = getFiel;
        this.fielRepository = fielRepository;
    }

    public AjusteCadastralController(
            GerenciarAjusteCadastralUseCase useCase,
            EventoActorContextResolver ator,
            UsuarioJpaRepository usuarios,
            ObterFielUseCase getFiel) {
        this(useCase, ator, usuarios, getFiel, null);
    }

    @GetMapping("/meu-perfil")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém dados do meu perfil de fiel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = FielResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuário ou fiel vinculado não encontrado")
    })
    public FielResponse meuPerfil() {
        UUID usuarioId = ator.resolveRequired().usuarioId();
        UUID fielId = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_USUARIO_NAO_ENCONTRADO))
                .getFielId();
        return getFiel.execute(fielId);
    }

    @GetMapping("/meu-perfil/portabilidade")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Exporta todos os dados do titular em formato interoperável (Art. 18, V LGPD)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados para portabilidade exportados com sucesso",
                    content = @Content(schema = @Schema(implementation = PortabilidadeTitularResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Fiel vinculado não encontrado")
    })
    public PortabilidadeTitularResponse portabilidade() {
        UUID usuarioId = ator.resolveRequired().usuarioId();
        UUID fielId = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_USUARIO_NAO_ENCONTRADO))
                .getFielId();
        FielResponse dados = getFiel.execute(fielId);
        return PortabilidadeTitularResponse.de(dados);
    }

    @PatchMapping("/meu-perfil/preferencias-privacidade")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Altera ou revoga preferências de consentimento secundárias do titular (Art. 8º, § 5º LGPD)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Preferências atualizadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Fiel vinculado não encontrado")
    })
    public void atualizarPreferencias(@RequestBody PreferenciasPrivacidadeRequest request) {
        if (fielRepository == null) {
            return;
        }
        UUID usuarioId = ator.resolveRequired().usuarioId();
        UUID fielId = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException(MSG_USUARIO_NAO_ENCONTRADO))
                .getFielId();
        var fiel = fielRepository.findById(fielId)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado."));
        if (request.usoImagemAutorizado() != null) {
            fiel.setUsoImagemAutorizado(request.usoImagemAutorizado());
        }
        if (request.permitirDivulgacaoAniversario() != null) {
            fiel.setPermitirDivulgacaoAniversario(request.permitirDivulgacaoAniversario());
        }
        fielRepository.save(fiel);
    }

    @PostMapping("/meu-perfil/solicitar-ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Solicita ajuste cadastral no próprio perfil")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação de ajuste criada com sucesso",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Dados do ajuste inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public UUID solicitar(@RequestBody @Valid SolicitarAjusteCadastralRequest request) {
        return useCase.solicitar(ator.resolveRequired().usuarioId(), request);
    }

    @GetMapping("/ajustes-cadastrais")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Lista solicitações de ajuste pendentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pendências recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = SolicitacaoAjusteCadastralResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<SolicitacaoAjusteCadastralResponse> pendentes() {
        return useCase.pendentes();
    }

    @GetMapping("/fieis/{fielId}/ajustes-cadastrais")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Lista solicitações pendentes de um fiel específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pendências do fiel recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = SolicitacaoAjusteCadastralResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<SolicitacaoAjusteCadastralResponse> pendentesDoFiel(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID fielId) {
        return useCase.pendentesDoFiel(fielId);
    }

    @PostMapping("/ajustes-cadastrais/{id}/decidir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Decide uma solicitação de ajuste cadastral")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Decisão registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da decisão inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public void decidir(
            @Parameter(description = "Identificador único da solicitação") @PathVariable UUID id,
            @RequestBody @Valid DecidirAjusteCadastralRequest request) {
        useCase.decidir(id, ator.resolveRequired().usuarioId(), request.aprovar(), request.justificativa());
    }
}
