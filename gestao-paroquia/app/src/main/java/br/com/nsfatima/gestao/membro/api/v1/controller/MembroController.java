package br.com.nsfatima.gestao.membro.api.v1.controller;

import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse;
import br.com.nsfatima.gestao.membro.application.usecase.AddParticipanteUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListParticipacoesUseCase;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.application.usecase.CreateMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.GetMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListMembrosUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.UpdateMembroUseCase;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/membros")
@Tag(name = "Membros", description = "Endpoints para gestão de fiéis e paroquianos")
public class MembroController {

    private final CreateMembroUseCase createMembroUseCase;
    private final UpdateMembroUseCase updateMembroUseCase;
    private final GetMembroUseCase getMembroUseCase;
    private final ListMembrosUseCase listMembrosUseCase;
    private final AddParticipanteUseCase addParticipanteUseCase;
    private final ListParticipacoesUseCase listParticipacoesUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public MembroController(
            CreateMembroUseCase createMembroUseCase,
            UpdateMembroUseCase updateMembroUseCase,
            GetMembroUseCase getMembroUseCase,
            ListMembrosUseCase listMembrosUseCase,
            AddParticipanteUseCase addParticipanteUseCase,
            ListParticipacoesUseCase listParticipacoesUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.createMembroUseCase = createMembroUseCase;
        this.updateMembroUseCase = updateMembroUseCase;
        this.getMembroUseCase = getMembroUseCase;
        this.listMembrosUseCase = listMembrosUseCase;
        this.addParticipanteUseCase = addParticipanteUseCase;
        this.listParticipacoesUseCase = listParticipacoesUseCase;
        this.actorContextResolver = actorContextResolver;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um novo membro")
    /**
     * Registra um novo fiel ou paroquiano no sistema para permitir o acompanhamento de sua vida sacramental e participação na comunidade.
     * 
     * Exemplo: POST /api/v1/membros { "nomeCompleto": "Maria Oliveira", "email": "maria@example.com", ... }
     */
    public MembroResponse create(@RequestBody @Valid MembroRequest request) {
        var context = actorContextResolver.resolveRequired();
        return createMembroUseCase.execute(request, context.actor());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista membros com filtros e paginação")
    /**
     * Recupera uma lista paginada de membros, permitindo a aplicação de filtros por nome e status para facilitar a gestão do cadastro.
     * 
     * Exemplo: GET /api/v1/membros?nome=Maria&ativo=true&page=0&size=20
     */
    public Page<MembroResponse> list(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        return listMembrosUseCase.execute(nome, ativo, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtém detalhes de um membro")
    /**
     * Busca as informações completas de um membro específico para consulta detalhada ou preparação de atualização de dados.
     * 
     * Exemplo: GET /api/v1/membros/{id}
     */
    public MembroResponse get(@PathVariable UUID id) {
        return getMembroUseCase.execute(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza dados de um membro")
    /**
     * Atualiza as informações cadastrais de um membro para manter o histórico sacramental e dados de contato fidedignos.
     * 
     * Exemplo: PATCH /api/v1/membros/{id} { "telefone": "(11) 99999-9999", "endereco": "Nova Rua, 123" }
     */
    public MembroResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid MembroRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateMembroUseCase.execute(id, request, context.actor());
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativa ou desativa um membro")
    /**
     * Altera a situação de atividade de um membro no sistema para refletir sua condição atual de participação na paróquia.
     * 
     * Exemplo: PATCH /api/v1/membros/{id}/status?ativo=false
     */
    public void toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean ativo) {
        var context = actorContextResolver.resolveRequired();
        updateMembroUseCase.toggleAtivo(id, ativo, context.actor());
    }

    @GetMapping("/{id}/organizacoes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista organizações que o membro participa")
    /**
     * Lista as pastorais, movimentos ou grupos aos quais o membro está vinculado para entender seu engajamento e responsabilidades.
     * 
     * Exemplo: GET /api/v1/membros/{id}/organizacoes
     */
    public List<ParticipacaoResponse> listOrganizacoes(@PathVariable UUID id) {
        return listParticipacoesUseCase.execute(id);
    }

    @PostMapping("/{id}/organizacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inscreve um membro em uma organização")
    /**
     * Vincula um membro a uma organização específica, registrando formalmente o início de sua participação ativa naquele grupo.
     * 
     * Exemplo: POST /api/v1/membros/{id}/organizacoes { "organizacaoId": "...", "dataInicio": "2023-01-01" }
     */
    public ParticipacaoResponse addOrganizacao(
            @PathVariable UUID id,
            @RequestBody @Valid ParticipacaoRequest request) {
        var context = actorContextResolver.resolveRequired();
        return addParticipanteUseCase.execute(id, request, context.actor());
    }
}
