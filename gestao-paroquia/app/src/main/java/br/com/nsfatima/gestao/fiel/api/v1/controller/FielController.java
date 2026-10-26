package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.domain.policy.PoliticaVisaoFiel;
import br.com.nsfatima.gestao.fiel.api.v1.dto.CriarFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielConsultaResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.AlterarStatusFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.AtualizarFielRequest;
import br.com.nsfatima.gestao.fiel.application.usecase.CriarFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.ObterFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.ListarFieisUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.AtualizarFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarCicloVidaFielUseCase;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.springframework.security.access.AccessDeniedException;
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

    private final CriarFielUseCase createFielUseCase;
    private final ObterFielUseCase getFielUseCase;
    private final ListarFieisUseCase listFieisUseCase;
    private final AtualizarFielUseCase updateFielUseCase;
    private final GerenciarCicloVidaFielUseCase cicloVidaFielUseCase;
    private final EventoActorContextResolver actorContextResolver;
    private final MembroGrupoJpaRepository membroGrupoRepository;

    public FielController(
            CriarFielUseCase createFielUseCase,
            ObterFielUseCase getFielUseCase,
            ListarFieisUseCase listFieisUseCase,
            AtualizarFielUseCase updateFielUseCase,
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
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'VIGARIO', 'SECRETARIA_PAROQUIAL', 'COORDENADOR', 'SECRETARIO_CONSELHO', 'MEMBRO_CONSELHO_PASTORAL')")
    @Operation(summary = "Lista todos os Fiéis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de fiéis recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = FielConsultaResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<? extends FielConsultaResponse> listar() {
        var context = actorContextResolver.resolveRequired();
        UUID grupo = context.organizationId();
        return switch (PoliticaVisaoFiel.para(context.role())) {
            case COMPLETA -> listFieisUseCase.execute();
            case CONTATO -> grupo == null ? List.of() : listFieisUseCase.executeContatoNoGrupo(grupo);
            case NOME -> grupo == null ? listFieisUseCase.executeNomes() : listFieisUseCase.executeNomesNoGrupo(grupo);
        };
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'VIGARIO', 'SECRETARIA_PAROQUIAL', 'COORDENADOR', 'SECRETARIO_CONSELHO', 'MEMBRO_CONSELHO_PASTORAL')")
    @Operation(summary = "Obtém detalhes de um Fiel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fiel recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = FielConsultaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel não encontrado")
    })
    public FielConsultaResponse obter(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID id) {
        var context = actorContextResolver.resolveRequired();
        var visao = PoliticaVisaoFiel.para(context.role());
        if (visao != PoliticaVisaoFiel.Visao.COMPLETA && !podeConsultar(visao, id, context.organizationId())) {
            throw new AccessDeniedException("Só é possível consultar fiéis vinculados à sua organização");
        }
        return switch (visao) {
            case COMPLETA -> getFielUseCase.execute(id);
            case CONTATO -> getFielUseCase.executeContato(id);
            case NOME -> getFielUseCase.executeNome(id);
        };
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'VIGARIO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Cadastra um novo Fiel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fiel cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = FielResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados cadastrais inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Conflito de CPF cadastrado")
    })
    public FielResponse criar(@RequestBody @Valid CriarFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return createFielUseCase.execute(request, context.actor());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'VIGARIO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Atualiza um Fiel (substituição)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fiel atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = FielResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel não encontrado")
    })
    public FielResponse atualizar(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID id,
            @RequestBody AtualizarFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateFielUseCase.execute(id, request, context.actor());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'VIGARIO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Atualiza um Fiel (parcial)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campos do fiel atualizados com sucesso",
                    content = @Content(schema = @Schema(implementation = FielResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados parciais inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel não encontrado")
    })
    public FielResponse atualizarParcial(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID id,
            @RequestBody AtualizarFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateFielUseCase.execute(id, request, context.actor());
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Ativa ou desativa um cadastro de Fiel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status do fiel alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel não encontrado")
    })
    public void alterarStatus(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID id,
            @RequestBody @Valid AlterarStatusFielRequest request) {
        var context = actorContextResolver.resolveRequired();
        if (Boolean.TRUE.equals(request.ativo())) {
            cicloVidaFielUseCase.reativar(id, context.actor());
            return;
        }
        cicloVidaFielUseCase.desativar(id, context.actor());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Exclui logicamente um cadastro de Fiel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Fiel inativado/excluído logicamente com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel não encontrado")
    })
    public void desativar(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID id) {
        var context = actorContextResolver.resolveRequired();
        cicloVidaFielUseCase.desativar(id, context.actor());
    }

    private boolean podeConsultar(PoliticaVisaoFiel.Visao visao, UUID fielId, UUID organizacaoId) {
        if (organizacaoId == null) {
            return visao != PoliticaVisaoFiel.Visao.CONTATO;
        }
        return membroGrupoRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId).isPresent();
    }
}
