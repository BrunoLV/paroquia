package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.UpdateFielRequest;
import br.com.nsfatima.gestao.fiel.application.usecase.CreateFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.GetFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.ListFieisUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.UpdateFielUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Endpoints REST para gerenciamento de Fiéis (paroquianos).
 */
@RestController
@RequestMapping("/api/v1/fieis")
@Tag(name = "Fiéis", description = "Endpoints para gerenciamento de Fiéis (pessoas físicas no Contexto Paroquial)")
public class FielController {

    private final CreateFielUseCase createFielUseCase;
    private final GetFielUseCase getFielUseCase;
    private final ListFieisUseCase listFieisUseCase;
    private final UpdateFielUseCase updateFielUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public FielController(
            CreateFielUseCase createFielUseCase,
            GetFielUseCase getFielUseCase,
            ListFieisUseCase listFieisUseCase,
            UpdateFielUseCase updateFielUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.createFielUseCase = createFielUseCase;
        this.getFielUseCase = getFielUseCase;
        this.listFieisUseCase = listFieisUseCase;
        this.updateFielUseCase = updateFielUseCase;
        this.actorContextResolver = actorContextResolver;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'CONSELHO_COORDENADOR', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Lista todos os Fiéis")
    public List<FielResponse> list() {
        return listFieisUseCase.execute();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'CONSELHO_COORDENADOR', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Obtém detalhes de um Fiel")
    public FielResponse get(@PathVariable UUID id) {
        return getFielUseCase.execute(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'CONSELHO_COORDENADOR', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Cadastra um novo Fiel")
    public FielResponse create(@RequestBody @Valid CreateFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return createFielUseCase.execute(request, context.actor());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'CONSELHO_COORDENADOR', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Atualiza um Fiel (substituição)")
    public FielResponse updatePut(@PathVariable UUID id, @RequestBody UpdateFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateFielUseCase.execute(id, request, context.actor());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'CONSELHO_COORDENADOR', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Atualiza um Fiel (parcial)")
    public FielResponse updatePatch(@PathVariable UUID id, @RequestBody UpdateFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateFielUseCase.execute(id, request, context.actor());
    }
}
