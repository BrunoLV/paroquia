package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaRequestDTO;
import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaResponseDTO;
import br.com.nsfatima.gestao.calendario.application.service.SolenidadeLiturgicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Gerencia o cadastro das solenidades litúrgicas (datas fixas ou móveis do calendário eclesiástico,
 * como Natal, Páscoa e festas de guarda) que servem de base para o provisionamento anual de eventos
 * da Matriz e das Comunidades. A criação, atualização e remoção são restritas ao Pároco via
 * segurança declarativa {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/v1/solenidades-liturgicas")
@Tag(name = "Solenidades Litúrgicas", description = "Endpoints para gerenciamento de solenidades e festas litúrgicas da Igreja")
@PreAuthorize("isAuthenticated()")
public class SolenidadeLiturgicaController {

    private final SolenidadeLiturgicaService service;

    public SolenidadeLiturgicaController(SolenidadeLiturgicaService service) {
        this.service = service;
    }

    /**
     * Lista todas as solenidades litúrgicas cadastradas, incluindo as inativas, para telas de
     * administração e manutenção do calendário eclesiástico.
     *
     * @return todas as solenidades litúrgicas cadastradas
     */
    @GetMapping
    @Operation(summary = "Lista todas as solenidades",
            description = "Retorna todas as solenidades litúrgicas cadastradas, incluindo inativas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = SolenidadeLiturgicaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<SolenidadeLiturgicaResponseDTO>> listar() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Lista apenas as solenidades litúrgicas atualmente ativas, usadas pelo provisionamento anual
     * de eventos e por telas de consulta do calendário corrente.
     *
     * @return as solenidades litúrgicas com status ativo
     */
    @GetMapping("/ativas")
    @Operation(summary = "Lista solenidades ativas",
            description = "Retorna apenas as solenidades litúrgicas com status ativo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solenidades ativas recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = SolenidadeLiturgicaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<SolenidadeLiturgicaResponseDTO>> listarAtivas() {
        return ResponseEntity.ok(service.findAtivas());
    }

    /**
     * Cadastra uma nova solenidade litúrgica, restrita à autoridade canônica do Pároco.
     *
     * @param request dados da solenidade a ser criada
     * @return a solenidade litúrgica criada, com status HTTP 201
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLERO_PAROCO', 'GLOBAL_PAROCO', 'PAROCO')")
    @Operation(summary = "Cadastra uma nova solenidade", description = "Cria uma nova solenidade litúrgica com base nos dados informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solenidade criada com sucesso",
                    content = @Content(schema = @Schema(implementation = SolenidadeLiturgicaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<SolenidadeLiturgicaResponseDTO> criar(
            @RequestBody SolenidadeLiturgicaRequestDTO request) {
        SolenidadeLiturgicaResponseDTO response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Atualiza os dados de uma solenidade litúrgica existente, restrita à autoridade canônica do Pároco.
     *
     * @param id identificador da solenidade a ser atualizada
     * @param request novos dados da solenidade
     * @return a solenidade litúrgica atualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLERO_PAROCO', 'GLOBAL_PAROCO', 'PAROCO')")
    @Operation(summary = "Atualiza uma solenidade", description = "Atualiza os dados de uma solenidade litúrgica existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solenidade atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = SolenidadeLiturgicaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Solenidade não encontrada")
    })
    public ResponseEntity<SolenidadeLiturgicaResponseDTO> atualizar(
            @Parameter(description = "Identificador único da solenidade") @PathVariable UUID id,
            @RequestBody SolenidadeLiturgicaRequestDTO request) {
        SolenidadeLiturgicaResponseDTO response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove uma solenidade litúrgica do cadastro, restrita à autoridade canônica do Pároco.
     * Solenidades removidas deixam de ser consideradas no provisionamento anual de eventos.
     *
     * @param id identificador da solenidade a ser removida
     * @return {@code 204 No Content} após a remoção bem-sucedida
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLERO_PAROCO', 'GLOBAL_PAROCO', 'PAROCO')")
    @Operation(summary = "Remove uma solenidade", description = "Remove uma solenidade litúrgica pelo identificador.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solenidade removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Solenidade não encontrada")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador único da solenidade") @PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
