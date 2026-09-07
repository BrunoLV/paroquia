package br.com.nsfatima.gestao.clero.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.DecidirAjusteClericalRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.EncargoClericalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.SolicitacaoAjusteClericalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.SolicitarAjusteClericalRequest;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarAjusteClericalUseCase;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarClerigoUseCase;
import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import br.com.nsfatima.gestao.fiel.api.v1.dto.CriarRelacionamentoFamiliarRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.RelacionamentoFamiliarResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarRelacionamentoFamiliarUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints protegidos de gestão, autoatendimento e diretório institucional do Clero. */
@RestController
@RequestMapping("/api/v1/clerigos")
@Tag(name = "Clero", description = "Gestão institucional de ministros ordenados")
public class ClerigoController {

    private final GerenciarClerigoUseCase useCase;
    private final GerenciarAjusteClericalUseCase ajusteUseCase;
    private final GerenciarRelacionamentoFamiliarUseCase familiaUseCase;
    private final EventoActorContextResolver actor;

    public ClerigoController(
            GerenciarClerigoUseCase useCase,
            GerenciarAjusteClericalUseCase ajusteUseCase,
            GerenciarRelacionamentoFamiliarUseCase familiaUseCase,
            EventoActorContextResolver actor) {
        this.useCase = useCase;
        this.ajusteUseCase = ajusteUseCase;
        this.familiaUseCase = familiaUseCase;
        this.actor = actor;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista clérigos ativos")
    public List<ClerigoResponse> listar() {
        return useCase.listar();
    }

    @GetMapping("/institucional")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista o diretório institucional sem PII")
    public List<ClerigoInstitucionalResponse> diretorio() {
        return useCase.diretorio();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém perfil do clérigo autenticado")
    public ClerigoResponse perfilProprio() {
        return useCase.obterPorUsuario(actor.resolveRequired().usuarioId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Obtém cadastro clerical")
    public ClerigoResponse obter(@PathVariable UUID id) {
        return useCase.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Cadastra clérigo")
    public ClerigoResponse criar(@RequestBody @Valid ClerigoRequest request) {
        return useCase.criar(request, actor.resolveRequired().actor());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Atualiza clérigo")
    public ClerigoResponse atualizar(@PathVariable UUID id, @RequestBody @Valid ClerigoRequest request) {
        return useCase.atualizar(id, request, actor.resolveRequired().actor());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Inativa logicamente clérigo")
    public void inativar(@PathVariable UUID id) {
        var context = actor.resolveRequired();
        useCase.inativar(id, context.actor(), context.usuarioId());
    }

    @GetMapping("/{id}/historico-encargos")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista histórico de encargos e provisões do clérigo")
    public List<EncargoClericalResponse> listarEncargos(@PathVariable UUID id) {
        return useCase.listarEncargos(id);
    }

    @GetMapping("/solicitacoes-ajuste")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista solicitações de ajuste cadastral pendentes")
    public List<SolicitacaoAjusteClericalResponse> listarSolicitacoesAjuste() {
        return ajusteUseCase.listarPendentes();
    }

    @PostMapping("/solicitacoes-ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submete solicitação de ajuste cadastral pelo próprio clérigo")
    public UUID solicitarAjuste(@RequestBody @Valid SolicitarAjusteClericalRequest request) {
        return ajusteUseCase.solicitar(actor.resolveRequired().usuarioId(), request);
    }

    @PostMapping("/solicitacoes-ajuste/{id}/decisao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Aprova ou rejeita solicitação de ajuste clerical")
    public void decidirAjuste(@PathVariable UUID id, @RequestBody @Valid DecidirAjusteClericalRequest request) {
        ajusteUseCase.decidir(id, actor.resolveRequired().usuarioId(), request.aprovar(), request.justificativa());
    }

    @GetMapping("/{clerigoId}/familiares")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista vínculos familiares do clérigo")
    public List<RelacionamentoFamiliarResponse> listarFamiliares(@PathVariable UUID clerigoId) {
        return familiaUseCase.listar(clerigoId, TipoPessoaFamiliar.CLERIGO);
    }

    @PostMapping("/{clerigoId}/familiares")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Adiciona vínculo familiar para o clérigo")
    public UUID adicionarFamiliar(
            @PathVariable UUID clerigoId,
            @RequestBody @Valid CriarRelacionamentoFamiliarRequest request) {
        return familiaUseCase.criar(
                clerigoId,
                TipoPessoaFamiliar.CLERIGO,
                request.pessoaDestinoId(),
                request.tipoPessoaDestinoEfetivo(),
                request.tipo(),
                actor.resolveRequired().usuarioId());
    }

    @DeleteMapping("/{clerigoId}/familiares/{relacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Remove vínculo familiar do clérigo")
    public void removerFamiliar(@PathVariable UUID clerigoId, @PathVariable UUID relacaoId) {
        familiaUseCase.remover(relacaoId, actor.resolveRequired().usuarioId());
    }
}
