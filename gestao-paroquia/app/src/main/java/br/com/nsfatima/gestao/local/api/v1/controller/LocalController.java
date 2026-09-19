package br.com.nsfatima.gestao.local.api.v1.controller;

import br.com.nsfatima.gestao.local.api.v1.dto.LocalRequest;
import br.com.nsfatima.gestao.local.api.v1.dto.LocalResponse;
import br.com.nsfatima.gestao.local.domain.model.EnderecoLocal;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.application.service.LocalService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Endpoints REST para gerenciamento de espaços físicos e dependências paroquiais.
 */
@RestController
@RequestMapping("/api/v1/locais")
@Tag(name = "Locais", description = "Gerenciamento de locais da paróquia")
public class LocalController {

    private final LocalService localService;

    /**
     * Inicializa o controller de locais com o serviço de domínio necessário para gestão de espaços físicos.
     *
     * @param localService Serviço para operações de domínio de locais
     */
    public LocalController(LocalService localService) {
        this.localService = localService;
    }

    /**
     * Registra um novo local físico na paróquia para possibilitar o agendamento de eventos e controle de
     * infraestrutura.
     *
     * @param request Dados do novo local
     * @return Dados do local criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Cria um novo local", description = "Registra um novo espaço físico para a paróquia.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Local criado com sucesso",
                    content = @Content(schema = @Schema(implementation = LocalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public LocalResponse criar(@RequestBody @Valid LocalRequest request) {
        Local local = localService.criar(
                request.nome(),
                request.tipo(),
                request.toEnderecoLocal(),
                request.capacidadeMaxima(),
                request.descricao(),
                request.igrejaId()
        );
        return toResponse(local);
    }

    /**
     * Atualiza as informações de um local existente para refletir mudanças na infraestrutura ou disponibilidade física.
     *
     * @param id      ID único do local
     * @param request Novos dados do local
     * @return Dados atualizados do local
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Atualiza um local existente", description = "Altera os dados cadastrais e de capacidade de um local.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Local atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LocalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado")
    })
    public LocalResponse atualizar(
            @Parameter(description = "Identificador único do local", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid LocalRequest request) {
        Local local = localService.atualizar(
                id,
                request.nome(),
                request.tipo(),
                request.toEnderecoLocal(),
                request.capacidadeMaxima(),
                request.descricao(),
                request.ativo(),
                request.igrejaId()
        );
        return toResponse(local);
    }

    /**
     * Remove logicamente (soft delete) um local do sistema, garantindo integridade com eventos futuros.
     *
     * @param id ID único do local a ser removido
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Exclui um local", description = "Exclui logicamente um local sem eventos futuros agendados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Local excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível excluir local com eventos futuros"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado")
    })
    public void excluir(
            @Parameter(description = "Identificador único do local", required = true)
            @PathVariable UUID id) {
        localService.excluir(id);
    }

    /**
     * Provê acesso aos detalhes de um local específico para consulta de sua capacidade e características técnicas.
     *
     * @param id ID único do local
     * @return Detalhes do local ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém detalhes de um local", description = "Retorna os dados cadastrais de um local pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Local encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = LocalResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado")
    })
    public ResponseEntity<LocalResponse> obter(
            @Parameter(description = "Identificador único do local", required = true)
            @PathVariable UUID id) {
        return localService.obterPorId(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos os locais disponíveis não excluídos para planejar eventos e reuniões.
     *
     * @param grupoParoquialId ID opcional de filtragem por paróquia/comunidade
     * @param igrejaId         Alias para grupoParoquialId
     * @return Lista de locais cadastrados
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todos os locais", description = "Retorna a lista de locais cadastrados, com filtro opcional por organização.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de locais retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LocalResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<LocalResponse> listar(
            @Parameter(description = "Filtro opcional por organização/grupo paroquial")
            @RequestParam(required = false) UUID grupoParoquialId,
            @Parameter(description = "Alias para grupoParoquialId")
            @RequestParam(required = false) UUID igrejaId) {
        UUID idFiltro = igrejaId != null ? igrejaId : grupoParoquialId;
        return localService.listar(idFiltro).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LocalResponse toResponse(Local domain) {
        EnderecoLocal el = domain.getEndereco();
        return new LocalResponse(
                domain.getId(),
                domain.getNome(),
                domain.getTipo(),
                domain.getCapacidadeMaxima(),
                domain.getDescricao(),
                domain.isAtivo(),
                domain.getIgrejaId(),
                domain.getIgrejaNome(),
                el != null ? el.cep() : null,
                el != null ? el.logradouro() : null,
                el != null ? el.numero() : null,
                el != null ? el.complemento() : null,
                el != null ? el.bairro() : null,
                el != null ? el.cidade() : null,
                el != null ? el.uf() : null,
                el != null ? el.formatado() : null,
                domain.getDeletedAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
