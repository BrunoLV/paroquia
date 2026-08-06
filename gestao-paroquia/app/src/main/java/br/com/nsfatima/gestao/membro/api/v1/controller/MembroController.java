package br.com.nsfatima.gestao.membro.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorResponse;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.GetMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListMembrosUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.PromoverACoordenadorUseCase;
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

import java.util.UUID;

/**
 * Expõe os endpoints REST de gestão de vínculos Fiel-organização (membros). Todas as operações exigem papel
 * {@code ADMIN} (exceto a promoção a coordenador, restrita a Pároco/Vigário/Mesa do CPP/Admin — ver
 * {@link PromoverACoordenadorUseCase}) e as mutações resolvem o ator autenticado via
 * {@link EventoActorContextResolver} para fins de auditoria. Participação em múltiplas organizações é
 * simplesmente múltiplas chamadas a {@code POST /}, uma por organização — não há mais um recurso separado de
 * "participações".
 */
@RestController
@RequestMapping("/api/v1/membros")
@Tag(name = "Membros", description = "Endpoints para gestão de fiéis e paroquianos")
public class MembroController {

    private final AdicionarMembroUseCase adicionarMembroUseCase;
    private final UpdateMembroUseCase updateMembroUseCase;
    private final GetMembroUseCase getMembroUseCase;
    private final ListMembrosUseCase listMembrosUseCase;
    private final PromoverACoordenadorUseCase promoverACoordenadorUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public MembroController(
            AdicionarMembroUseCase adicionarMembroUseCase,
            UpdateMembroUseCase updateMembroUseCase,
            GetMembroUseCase getMembroUseCase,
            ListMembrosUseCase listMembrosUseCase,
            PromoverACoordenadorUseCase promoverACoordenadorUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.adicionarMembroUseCase = adicionarMembroUseCase;
        this.updateMembroUseCase = updateMembroUseCase;
        this.getMembroUseCase = getMembroUseCase;
        this.listMembrosUseCase = listMembrosUseCase;
        this.promoverACoordenadorUseCase = promoverACoordenadorUseCase;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Cria o vínculo de um Fiel a uma organização com o papel informado. Nunca exige, lê ou cria um
     * {@code Usuario} — cadastro de membro comum não requer login.
     *
     * @param request dados do vínculo a ser criado
     * @return o vínculo criado, já com os dados de exibição do Fiel/organização resolvidos
     * @throws IllegalArgumentException se fiel/organização não existirem, o papel for inválido, ou já houver
     *                                   vínculo ativo entre eles
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra um novo membro")
    public MembroResponse create(@RequestBody @Valid MembroRequest request) {
        var context = actorContextResolver.resolveRequired();
        return adicionarMembroUseCase.execute(request, context.actor());
    }

    /**
     * Lista vínculos Fiel-organização, aplicando filtros opcionais por organização, Fiel e situação
     * (ativo/inativo).
     *
     * @param grupoParoquialId filtro por organização, ou {@code null} para não filtrar
     * @param fielId        filtro por Fiel, ou {@code null} para não filtrar
     * @param ativo         filtro por situação do vínculo, ou {@code null} para não filtrar
     * @param pageable      paginação/ordenação solicitada
     * @return página de vínculos que atendem aos filtros informados
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista membros com filtros e paginação")
    public Page<MembroResponse> list(
            @RequestParam(required = false) UUID grupoParoquialId,
            @RequestParam(required = false) UUID fielId,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        return listMembrosUseCase.execute(grupoParoquialId, fielId, ativo, pageable);
    }

    /**
     * Busca um vínculo Fiel-organização pelo identificador.
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
     * Atualiza os dados de um vínculo Fiel-organização existente. Repete a validação de unicidade de vínculo
     * ativo e ressincroniza o campo de liderança da organização quando o papel for de coordenação.
     *
     * @param id      identificador do vínculo a atualizar
     * @param request novos dados do vínculo
     * @return o vínculo atualizado
     * @throws IllegalArgumentException se o vínculo, fiel ou organização não existirem, ou se o fiel/
     *                                   organização já tiver outro vínculo ativo
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
     * Ativa ou desativa um vínculo Fiel-organização, sem alterar os demais dados cadastrais.
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
     * Promove um Fiel já membro ativo de uma organização a um papel de liderança, criando um login para ele
     * se ainda não possuir um.
     *
     * @param request dados da promoção
     * @return o vínculo atualizado e o {@code usuarioId} (existente ou recém-criado)
     */
    @PostMapping("/promover-coordenador")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLERO_PAROCO') or hasRole('CLERO_VIGARIO')"
            + " or hasRole('GLOBAL_ADMIN') or hasRole('GLOBAL_PAROCO')"
            + " or hasRole('GLOBAL_VIGARIO') or hasRole('GLOBAL_SECRETARIA_PAROQUIAL') or hasRole('CPP_MESA_CPP')")
    @Operation(summary = "Promove um Fiel membro a coordenador, criando login se necessário")
    public PromoverACoordenadorResponse promoverACoordenador(@RequestBody @Valid PromoverACoordenadorRequest request) {
        return promoverACoordenadorUseCase.execute(request);
    }
}
