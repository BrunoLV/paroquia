package br.com.nsfatima.gestao.governanca.api.v1.controller;

import br.com.nsfatima.gestao.governanca.api.v1.dto.AtualizarStatusIncidenteRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.IncidenteSegurancaResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.RegistrarIncidenteRequest;
import br.com.nsfatima.gestao.governanca.application.usecase.GerenciarIncidenteSegurancaUseCase;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints restritos à administração eclesiástica para governança e resposta a incidentes de segurança (Art. 48 LGPD).
 */
@RestController
@RequestMapping("/api/v1/governanca/incidentes")
@Tag(name = "Governança - Incidentes de Segurança LGPD", description = "Endpoints para registro, acompanhamento e despacho de incidentes e violações de dados")
@PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
public class IncidenteSegurancaController {

    private final GerenciarIncidenteSegurancaUseCase useCase;

    public IncidenteSegurancaController(GerenciarIncidenteSegurancaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra um novo incidente de segurança ou violação de dados")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Incidente registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = IncidenteSegurancaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso restrito ao Pároco ou Administrador")
    })
    public ResponseEntity<IncidenteSegurancaResponse> registrar(@Valid @RequestBody RegistrarIncidenteRequest request) {
        IncidenteSegurancaResponse response = useCase.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os incidentes de segurança registrados em ordem cronológica de detecção")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de incidentes recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = IncidenteSegurancaResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<IncidenteSegurancaResponse>> listar() {
        return ResponseEntity.ok(useCase.listarTodos());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza o status, medidas de mitigação ou despacho de notificação do incidente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incidente atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = IncidenteSegurancaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de atualização inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Incidente não encontrado")
    })
    public ResponseEntity<IncidenteSegurancaResponse> atualizar(
            @Parameter(description = "Identificador único do incidente") @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusIncidenteRequest request) {
        return ResponseEntity.ok(useCase.atualizarStatus(id, request));
    }
}
