package br.com.nsfatima.gestao.membro.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse;
import br.com.nsfatima.gestao.membro.application.usecase.AddParticipanteUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.CreateMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.GetMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListMembrosUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListParticipacoesUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.UpdateMembroUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Expõe os endpoints REST de gestão de vínculos usuário-organização (membros) e de suas participações
 * adicionais em outras organizações. Todas as operações exigem papel {@code ADMIN} e as mutações resolvem o
 * ator autenticado via {@link EventoActorContextResolver} para fins de auditoria.
 */
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

    /**
     * Cria o vínculo de um usuário a uma organização com o papel informado. Se o papel for
     * {@code COORDENADOR} ou {@code VICE_COORDENADOR}, sincroniza automaticamente o campo de liderança
     * correspondente na organização.
     *
     * @param request dados do vínculo a ser criado
     * @return o vínculo criado, já com os dados de exibição do usuário/organização resolvidos
     * @throws IllegalArgumentException se usuário/organização não existirem ou já houver vínculo ativo entre eles
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um novo membro")
    public MembroResponse create(@RequestBody @Valid MembroRequest request) {
        var context = actorContextResolver.resolveRequired();
        return createMembroUseCase.execute(request, context.actor());
    }

    /**
     * Lista vínculos usuário-organização, aplicando filtros opcionais por organização, usuário e situação
     * (ativo/inativo).
     *
     * @param organizacaoId filtro por organização, ou {@code null} para não filtrar
     * @param usuarioId     filtro por usuário, ou {@code null} para não filtrar
     * @param ativo         filtro por situação do vínculo, ou {@code null} para não filtrar
     * @param pageable      paginação/ordenação solicitada
     * @return página de vínculos que atendem aos filtros informados
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista membros com filtros e paginação")
    public Page<MembroResponse> list(
            @RequestParam(required = false) UUID organizacaoId,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        return listMembrosUseCase.execute(organizacaoId, usuarioId, ativo, pageable);
    }


    /**
     * Busca um vínculo usuário-organização pelo identificador.
     *
     * @param id identificador do vínculo
     * @return o vínculo encontrado, com dados de exibição resolvidos
     * @throws IllegalArgumentException se não houver vínculo com o {@code id} informado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtém detalhes de um membro")
    public MembroResponse get(@PathVariable UUID id) {
        return getMembroUseCase.execute(id);
    }

    /**
     * Atualiza os dados de um vínculo usuário-organização existente. Repete a validação de unicidade de vínculo
     * ativo (impede associar o novo usuário/organização a outro vínculo já ativo diferente do que está sendo
     * editado) e ressincroniza o campo de liderança da organização quando o papel for de coordenação.
     *
     * @param id      identificador do vínculo a atualizar
     * @param request novos dados do vínculo
     * @return o vínculo atualizado
     * @throws IllegalArgumentException se o vínculo, usuário ou organização não existirem, ou se o
     *                                   usuário/organização já tiver outro vínculo ativo
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza dados de um membro")
    public MembroResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid MembroRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateMembroUseCase.execute(id, request, context.actor());
    }

    /**
     * Ativa ou desativa um vínculo usuário-organização, sem alterar os demais dados cadastrais.
     *
     * @param id    identificador do vínculo
     * @param ativo novo estado desejado ({@code true} para ativar, {@code false} para desativar)
     * @throws IllegalArgumentException se não houver vínculo com o {@code id} informado
     */
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativa ou desativa um membro")
    public void toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean ativo) {
        var context = actorContextResolver.resolveRequired();
        updateMembroUseCase.toggleAtivo(id, ativo, context.actor());
    }

    /**
     * Lista as participações ativas do membro (identificado pelo vínculo usuário-organização) em organizações
     * adicionais, para entender seu engajamento em pastorais/movimentos além do vínculo principal.
     *
     * @param id identificador do vínculo (membro)
     * @return participações ativas encontradas; lista vazia se não houver nenhuma
     */
    @GetMapping("/{id}/organizacoes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista organizações que o membro participa")
    public List<ParticipacaoResponse> listOrganizacoes(@PathVariable UUID id) {
        return listParticipacoesUseCase.execute(id);
    }

    /**
     * Inscreve o membro (vínculo usuário-organização) em uma organização adicional, desde que ele possua
     * usuário cadastrado e ainda não participe ativamente daquela organização.
     *
     * @param id      identificador do vínculo (membro) a inscrever
     * @param request organização de destino e data de início da participação
     * @return a participação criada
     * @throws IllegalArgumentException se o membro não existir, não possuir usuário associado, ou já
     *                                   participar ativamente da organização informada
     */
    @PostMapping("/{id}/organizacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Inscreve um membro em uma organização")
    public ParticipacaoResponse addOrganizacao(
            @PathVariable UUID id,
            @RequestBody @Valid ParticipacaoRequest request) {
        var context = actorContextResolver.resolveRequired();
        return addParticipanteUseCase.execute(id, request, context.actor());
    }
}
