package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.PastoralMovimentoOficialRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.PastoralMovimentoOficialResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.service.PastoralMovimentoOficialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pastorais-oficiais")
@Tag(name = "Official Pastorals and Movements Catalog", description = "Management of official catalog of Pastorals and Movements")
public class PastoralMovimentoOficialController {

    private final PastoralMovimentoOficialService service;

    public PastoralMovimentoOficialController(PastoralMovimentoOficialService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new official pastoral/movement", description = "Only admins can manage the official catalog.")
    public PastoralMovimentoOficialResponse create(@RequestBody @Valid PastoralMovimentoOficialRequest request) {
        PastoralMovimentoOficial oficial = service.create(request.nome(), request.tipo());
        return toResponse(oficial);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing official pastoral/movement", description = "Only admins can manage the official catalog.")
    public PastoralMovimentoOficialResponse update(@PathVariable UUID id, @RequestBody @Valid PastoralMovimentoOficialRequest request) {
        PastoralMovimentoOficial oficial = service.update(id, request.nome(), request.tipo());
        return toResponse(oficial);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an official pastoral/movement", description = "Only admins can manage the official catalog.")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get official pastoral/movement by ID")
    public PastoralMovimentoOficialResponse getById(@PathVariable UUID id) {
        PastoralMovimentoOficial oficial = service.get(id)
                .orElseThrow(() -> new br.com.nsfatima.gestao.organizacao.domain.exception.PastoralMovimentoOficialBusinessException("Pastoral or Movimento Oficial not found with ID: " + id));
        return toResponse(oficial);
    }

    @GetMapping
    @Operation(summary = "List all official pastorals/movements")
    public List<PastoralMovimentoOficialResponse> list() {
        return service.list().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PastoralMovimentoOficialResponse toResponse(PastoralMovimentoOficial oficial) {
        return new PastoralMovimentoOficialResponse(
                oficial.getId(),
                oficial.getNome(),
                oficial.getTipo()
        );
    }
}
