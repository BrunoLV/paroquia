package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.UnidadeTerritorialRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.UnidadeTerritorialResponse;
import br.com.nsfatima.gestao.organizacao.application.service.UnidadeTerritorialService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Expõe a API REST de gestão de unidades territoriais da paróquia (Clero, Matriz, Comunidades).
 */
@RestController
@RequestMapping("/api/v1/unidades-territoriais")
@Tag(name = "Unidades Territoriais", description = "Gerenciamento de unidades territoriais (Clero, Matriz, Comunidades)")
public class UnidadeTerritorialController {

    private final UnidadeTerritorialService service;

    public UnidadeTerritorialController(UnidadeTerritorialService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todas as unidades territoriais", description = "Retorna a lista de todas as unidades territoriais cadastradas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de unidades territoriais retornada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = UnidadeTerritorialResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<UnidadeTerritorialResponse> listar() {
        return service.listar().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém detalhes de uma unidade territorial", description = "Retorna os dados cadastrais da unidade territorial pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade territorial encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = UnidadeTerritorialResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Unidade territorial não encontrada")
    })
    public ResponseEntity<UnidadeTerritorialResponse> obter(
            @Parameter(description = "Identificador único da unidade territorial", required = true)
            @PathVariable UUID id) {
        return service.obterPorId(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Cria uma nova unidade territorial", description = "Registra uma nova unidade territorial na paróquia.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidade territorial criada com sucesso",
                    content = @Content(schema = @Schema(implementation = UnidadeTerritorialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public UnidadeTerritorialResponse criar(@RequestBody @Valid UnidadeTerritorialRequest request) {
        UnidadeTerritorialEntity entity = service.criar(request);
        return toResponse(entity);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Atualiza uma unidade territorial", description = "Atualiza os dados de uma unidade territorial existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade territorial atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = UnidadeTerritorialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Unidade territorial não encontrada")
    })
    public UnidadeTerritorialResponse atualizar(
            @Parameter(description = "Identificador único da unidade territorial", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid UnidadeTerritorialRequest request) {
        UnidadeTerritorialEntity entity = service.atualizar(id, request);
        return toResponse(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Exclui uma unidade territorial", description = "Remove uma unidade territorial sem dependências.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unidade territorial excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Unidade territorial não encontrada")
    })
    public void excluir(
            @Parameter(description = "Identificador único da unidade territorial", required = true)
            @PathVariable UUID id) {
        service.excluir(id);
    }

    private UnidadeTerritorialResponse toResponse(UnidadeTerritorialEntity entity) {
        return new UnidadeTerritorialResponse(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                entity.getContato(),
                entity.isAtivo(),
                entity.getParentId(),

                // Canônicos
                entity.getPadroeiro(),
                entity.getDataFestaPadroeira(),
                entity.getDataDedicacaoAltar(),
                entity.isPossuiSacrario(),
                entity.isPossuiPiaBatismal(),
                entity.getClerigoResponsavelId(),
                null,

                // Fiscais e Jurídicos
                entity.getCnpj(),
                entity.getInscricaoMunicipal(),
                entity.getInscricaoImobiliariaIptu(),
                entity.getProcessoImunidadeIptu(),

                // Registro de Imóveis (RGI)
                entity.getCartorioRgi(),
                entity.getMatriculaImovel(),
                entity.getTipoAquisicao(),
                entity.getAreaTerrenoM2(),
                entity.getAreaConstruidaM2(),

                // Segurança e Concessionárias
                entity.getAvcbNumero(),
                entity.getAvcbValidade(),
                entity.getLotacaoMaximaBombeiros(),
                entity.getCodigoConsumidorEnergia(),
                entity.getCodigoConsumidorAgua(),

                // Contato e Endereço Estruturado
                entity.getTelefone(),
                entity.getEmail(),
                entity.getEnderecoCep(),
                entity.getEnderecoLogradouro(),
                entity.getEnderecoNumero(),
                entity.getEnderecoComplemento(),
                entity.getEnderecoBairro(),
                entity.getEnderecoCidade(),
                entity.getEnderecoUf()
        );
    }
}
