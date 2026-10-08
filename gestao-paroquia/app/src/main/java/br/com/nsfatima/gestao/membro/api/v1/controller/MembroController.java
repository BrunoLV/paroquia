package br.com.nsfatima.gestao.membro.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.membro.api.v1.dto.IdoneidadeRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.IdoneidadeResponse;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorResponse;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ObterMembroUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.ListarMembrosUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.PromoverACoordenadorUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.RegistrarIdoneidadeUseCase;
import br.com.nsfatima.gestao.membro.application.usecase.AtualizarMembroUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.PutMapping;
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
    private final AtualizarMembroUseCase atualizarMembroUseCase;
    private final ObterMembroUseCase obterMembroUseCase;
    private final ListarMembrosUseCase listarMembrosUseCase;
    private final PromoverACoordenadorUseCase promoverACoordenadorUseCase;
    private final RegistrarIdoneidadeUseCase registrarIdoneidadeUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public MembroController(
            AdicionarMembroUseCase adicionarMembroUseCase,
            AtualizarMembroUseCase atualizarMembroUseCase,
            ObterMembroUseCase obterMembroUseCase,
            ListarMembrosUseCase listarMembrosUseCase,
            PromoverACoordenadorUseCase promoverACoordenadorUseCase,
            RegistrarIdoneidadeUseCase registrarIdoneidadeUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.adicionarMembroUseCase = adicionarMembroUseCase;
        this.atualizarMembroUseCase = atualizarMembroUseCase;
        this.obterMembroUseCase = obterMembroUseCase;
        this.listarMembrosUseCase = listarMembrosUseCase;
        this.promoverACoordenadorUseCase = promoverACoordenadorUseCase;
        this.registrarIdoneidadeUseCase = registrarIdoneidadeUseCase;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Cadastra um novo membro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Membro vinculado com sucesso",
                    content = @Content(schema = @Schema(implementation = MembroResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados do membro inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public MembroResponse criar(@RequestBody @Valid MembroRequest request) {
        var context = actorContextResolver.resolveRequired();
        return adicionarMembroUseCase.execute(request, context.actor());
    }

    /**
     * Lista vínculos Fiel-organização, aplicando filtros opcionais por organização, Fiel e situação
     * (ativo/inativo).
     *
     * @param organizacaoId filtro por organização, ou {@code null} para não filtrar
     * @param fielId        filtro por Fiel, ou {@code null} para não filtrar
     * @param ativo         filtro por situação do vínculo, ou {@code null} para não filtrar
     * @param pageable      paginação/ordenação solicitada
     * @return página de vínculos que atendem aos filtros informados
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Lista membros com filtros e paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de membros recuperada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public Page<MembroResponse> listar(
            @Parameter(description = "ID do grupo paroquial") @RequestParam(required = false) UUID organizacaoId,
            @Parameter(description = "ID do fiel") @RequestParam(required = false) UUID fielId,
            @Parameter(description = "Status ativo do vínculo") @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20) Pageable pageable) {
        return listarMembrosUseCase.execute(organizacaoId, fielId, ativo, pageable);
    }

    /**
     * Busca um vínculo Fiel-organização pelo identificador.
     *
     * @param id identificador do vínculo
     * @return o vínculo encontrado, com dados de exibição resolvidos
     * @throws IllegalArgumentException se não houver vínculo com o {@code id} informado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Obtém detalhes de um membro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membro recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = MembroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public MembroResponse obter(
            @Parameter(description = "Identificador único do membro") @PathVariable UUID id) {
        return obterMembroUseCase.execute(id);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Atualiza dados de um membro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membro atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = MembroResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public MembroResponse atualizar(
            @Parameter(description = "Identificador único do membro") @PathVariable UUID id,
            @RequestBody @Valid MembroRequest request) {
        var context = actorContextResolver.resolveRequired();
        return atualizarMembroUseCase.execute(id, request, context.actor());
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
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Ativa ou desativa um membro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status do membro alterado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public void alternarStatus(
            @Parameter(description = "Identificador único do membro") @PathVariable UUID id,
            @Parameter(description = "Novo status de ativação") @RequestParam boolean ativo) {
        var context = actorContextResolver.resolveRequired();
        atualizarMembroUseCase.alternarAtivo(id, ativo, context.actor());
    }

    /**
     * Consulta a idoneidade pastoral do agente e as pendências para a função que ele ocupa.
     *
     * @param id identificador do vínculo
     * @return a idoneidade e as pendências
     */
    @GetMapping("/{id}/idoneidade")
    @PreAuthorize("hasAnyRole('PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Consulta a idoneidade pastoral de um agente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Idoneidade retornada",
                    content = @Content(schema = @Schema(implementation = IdoneidadeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Vínculo não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public IdoneidadeResponse obterIdoneidade(
            @Parameter(description = "Identificador do vínculo", required = true) @PathVariable UUID id) {
        return registrarIdoneidadeUseCase.obter(id);
    }

    /**
     * Registra a idoneidade pastoral do agente (formação, vivência sacramental, proteção de menores e envio).
     *
     * @param id identificador do vínculo
     * @param request datas atestadas
     * @return a idoneidade atualizada e as pendências
     */
    @PutMapping("/{id}/idoneidade")
    @PreAuthorize("hasAnyRole('PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Registra a idoneidade pastoral de um agente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Idoneidade registrada",
                    content = @Content(schema = @Schema(implementation = IdoneidadeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datas inválidas ou vínculo não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public IdoneidadeResponse registrarIdoneidade(
            @Parameter(description = "Identificador do vínculo", required = true) @PathVariable UUID id,
            @RequestBody @Valid IdoneidadeRequest request) {
        return registrarIdoneidadeUseCase.registrar(id, request, actorContextResolver.resolveRequired().actor());
    }

    /**
     * Promove um Fiel já membro ativo de uma organização a um papel de liderança, criando um login para ele
     * se ainda não possuir um. Restrito à provisão canônica do Pároco.
     *
     * @param request dados da promoção
     * @return o vínculo atualizado e o {@code usuarioId} (existente ou recém-criado)
     */
    @PostMapping("/promover-coordenador")
    @PreAuthorize("hasRole('PAROCO')")
    @Operation(summary = "Promove um Fiel membro a coordenador, criando login se necessário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membro promovido a coordenador com sucesso",
                    content = @Content(schema = @Schema(implementation = PromoverACoordenadorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da promoção inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public PromoverACoordenadorResponse promoverACoordenador(@RequestBody @Valid PromoverACoordenadorRequest request) {
        return promoverACoordenadorUseCase.execute(request);
    }
}
