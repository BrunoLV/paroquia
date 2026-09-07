package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielConsultaResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.AlterarStatusFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.UpdateFielRequest;
import br.com.nsfatima.gestao.fiel.application.usecase.CreateFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.GetFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.ListFieisUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.UpdateFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarCicloVidaFielUseCase;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final GerenciarCicloVidaFielUseCase cicloVidaFielUseCase;
    private final EventoActorContextResolver actorContextResolver;
    private final MembroGrupoJpaRepository membroGrupoRepository;

    public FielController(
            CreateFielUseCase createFielUseCase,
            GetFielUseCase getFielUseCase,
            ListFieisUseCase listFieisUseCase,
            UpdateFielUseCase updateFielUseCase,
            GerenciarCicloVidaFielUseCase cicloVidaFielUseCase,
            EventoActorContextResolver actorContextResolver,
            MembroGrupoJpaRepository membroGrupoRepository) {
        this.createFielUseCase = createFielUseCase;
        this.getFielUseCase = getFielUseCase;
        this.listFieisUseCase = listFieisUseCase;
        this.updateFielUseCase = updateFielUseCase;
        this.cicloVidaFielUseCase = cicloVidaFielUseCase;
        this.actorContextResolver = actorContextResolver;
        this.membroGrupoRepository = membroGrupoRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'CONSELHO_COORDENADOR', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Lista todos os Fiéis")
    public List<? extends FielConsultaResponse> list() {
        var context = actorContextResolver.resolveRequired();
        if (isCoordenador(context.role(), context.organizationId())) {
            return listFieisUseCase.executeContatoNoGrupo(context.organizationId());
        }
        if (context.organizationId() != null) {
            return listFieisUseCase.executeNoGrupo(context.organizationId());
        }
        return listFieisUseCase.execute();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'CONSELHO_COORDENADOR', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Obtém detalhes de um Fiel")
    public FielConsultaResponse get(@PathVariable UUID id) {
        var context = actorContextResolver.resolveRequired();
        if (context.organizationId() != null
                && membroGrupoRepository.findByFielIdAndGrupoParoquialIdAndAtivoTrue(id, context.organizationId()).isEmpty()) {
            throw new AccessDeniedException("Coordenador só pode consultar fiéis vinculados à sua organização");
        }
        if (isCoordenador(context.role(), context.organizationId())) {
            return getFielUseCase.executeContato(id);
        }
        return getFielUseCase.execute(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Cadastra um novo Fiel")
    public FielResponse create(@RequestBody @Valid CreateFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return createFielUseCase.execute(request, context.actor());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Atualiza um Fiel (substituição)")
    public FielResponse updatePut(@PathVariable UUID id, @RequestBody UpdateFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateFielUseCase.execute(id, request, context.actor());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CLERO_VIGARIO', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_VIGARIO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Atualiza um Fiel (parcial)")
    public FielResponse updatePatch(@PathVariable UUID id, @RequestBody UpdateFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateFielUseCase.execute(id, request, context.actor());
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Ativa ou desativa um cadastro de Fiel")
    public void alterarStatus(@PathVariable UUID id, @RequestBody @Valid AlterarStatusFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        if (Boolean.TRUE.equals(request.ativo())) {
            cicloVidaFielUseCase.reativar(id, context.actor());
            return;
        }
        cicloVidaFielUseCase.desativar(id, context.actor());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'GLOBAL_ADMIN', 'GLOBAL_PAROCO', 'GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Exclui logicamente um cadastro de Fiel")
    public void softDelete(@PathVariable UUID id) {
        var context = actorContextResolver.resolveRequired();
        cicloVidaFielUseCase.desativar(id, context.actor());
    }

    private boolean isCoordenador(String papel, UUID grupoParoquialId) {
        return grupoParoquialId != null && papel != null
                && papel.toUpperCase(java.util.Locale.ROOT).contains("COORDENADOR");
    }

}
