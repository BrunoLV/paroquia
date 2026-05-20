package br.com.nsfatima.gestao.local.api.v1.controller;

import br.com.nsfatima.gestao.local.api.v1.dto.LocalRequest;
import br.com.nsfatima.gestao.local.api.v1.dto.LocalResponse;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.service.LocalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Registra um novo local físico na paróquia para possibilitar o agendamento de eventos e controle de infraestrutura.
     *
     * <p>Usage Example:
     * {@code POST /api/v1/locais} com corpo JSON contendo nome, tipo, endereço e capacidade.
     *
     * @param request Dados do novo local
     * @return Dados do local criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new location", description = "Only administrators can create locations.")
    public LocalResponse create(@RequestBody @Valid LocalRequest request) {
        Local local = localService.createLocal(
                request.nome(),
                request.tipo(),
                request.endereco(),
                request.capacidade(),
                request.caracteristicas()
        );
        return toResponse(local);
    }

    /**
     * Atualiza as informações de um local existente para refletir mudanças na infraestrutura ou disponibilidade física.
     *
     * <p>Usage Example:
     * {@code PUT /api/v1/locais/{uuid}} com corpo JSON completo do local atualizado.
     *
     * @param id ID único do local
     * @param request Novos dados do local
     * @return Dados atualizados do local
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing location", description = "Only administrators can update locations.")
    public LocalResponse update(@PathVariable UUID id, @RequestBody @Valid LocalRequest request) {
        Local local = localService.updateLocal(
                id,
                request.nome(),
                request.tipo(),
                request.endereco(),
                request.capacidade(),
                request.caracteristicas(),
                request.ativo()
        );
        return toResponse(local);
    }

    /**
     * Remove um local do sistema para descontinuar o uso de espaços que não pertencem mais à paróquia ou estão inativos.
     *
     * <p>Usage Example:
     * {@code DELETE /api/v1/locais/{uuid}}
     *
     * @param id ID único do local a ser removido
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a location", description = "Only administrators can delete locations. Cannot delete locations in use.")
    public void delete(@PathVariable UUID id) {
        localService.deleteLocal(id);
    }

    /**
     * Provê acesso aos detalhes de um local específico para consulta de sua capacidade e características técnicas.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/locais/{uuid}}
     *
     * @param id ID único do local
     * @return Detalhes do local ou 404 se não encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get location details")
    public ResponseEntity<LocalResponse> get(@PathVariable UUID id) {
        return localService.getLocal(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos os locais disponíveis para facilitar o planejamento de eventos e a escolha de espaços adequados.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/locais}
     *
     * @return Lista de locais cadastrados
     */
    @GetMapping
    @Operation(summary = "List all locations")
    public List<LocalResponse> list() {
        return localService.listLocais().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LocalResponse toResponse(Local domain) {
        return new LocalResponse(
                domain.getId(),
                domain.getNome(),
                domain.getTipo(),
                domain.getEndereco(),
                domain.getCapacidade(),
                domain.getCaracteristicas(),
                domain.isAtivo()
        );
    }
}
