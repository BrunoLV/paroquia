package br.com.nsfatima.gestao.patrimonio.api.v1.controller;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoAlocacaoResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoOperationResult;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CancelarAlocacaoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CreateAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.DesalocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.IniciarManutencaoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.MetricasOciosidadeResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.TransferirAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.application.usecase.AlocarAtivoParaEventoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.CancelarAlocacaoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.CreateAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.DesalocarAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.FinalizarManutencaoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.GetMetricasOciosidadeUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.IniciarManutencaoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ListAlocacoesPorAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ListAlocacoesPorEventoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ListAtivosPorGrupoParoquialUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.TransferirAtivoPatrimonialUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Expõe o ciclo de vida de ativos patrimoniais (bens móveis da paróquia): cadastro, transferência
 * de organização dona, alocação/desalocação a eventos (com fluxo de aprovação quando a organização
 * do evento diverge da organização dona), manutenção e consulta de métricas de ociosidade.
 */
@RestController
@RequestMapping("/api/v1/ativos")
@Tag(name = "Patrimônio", description = "Endpoints para gestão de ativos patrimoniais")
public class AtivoPatrimonialController {

    private static final String AUTHENTICATED = "isAuthenticated()";

    private final CreateAtivoPatrimonialUseCase createUseCase;
    private final ListAtivosPorGrupoParoquialUseCase listUseCase;
    private final TransferirAtivoPatrimonialUseCase transferirUseCase;
    private final AlocarAtivoParaEventoUseCase alocarUseCase;
    private final DesalocarAtivoUseCase desalocarUseCase;
    private final IniciarManutencaoAtivoUseCase iniciarManutencaoUseCase;
    private final FinalizarManutencaoAtivoUseCase finalizarManutencaoUseCase;
    private final CancelarAlocacaoAtivoUseCase cancelarAlocacaoUseCase;
    private final ListAlocacoesPorAtivoUseCase listAlocacoesPorAtivoUseCase;
    private final ListAlocacoesPorEventoUseCase listAlocacoesPorEventoUseCase;
    private final GetMetricasOciosidadeUseCase metricasOciosidadeUseCase;

    public AtivoPatrimonialController(CreateAtivoPatrimonialUseCase createUseCase,
                                       ListAtivosPorGrupoParoquialUseCase listUseCase,
                                       TransferirAtivoPatrimonialUseCase transferirUseCase,
                                       AlocarAtivoParaEventoUseCase alocarUseCase,
                                       DesalocarAtivoUseCase desalocarUseCase,
                                       IniciarManutencaoAtivoUseCase iniciarManutencaoUseCase,
                                       FinalizarManutencaoAtivoUseCase finalizarManutencaoUseCase,
                                       CancelarAlocacaoAtivoUseCase cancelarAlocacaoUseCase,
                                       ListAlocacoesPorAtivoUseCase listAlocacoesPorAtivoUseCase,
                                       ListAlocacoesPorEventoUseCase listAlocacoesPorEventoUseCase,
                                       GetMetricasOciosidadeUseCase metricasOciosidadeUseCase) {
        this.createUseCase = createUseCase;
        this.listUseCase = listUseCase;
        this.transferirUseCase = transferirUseCase;
        this.alocarUseCase = alocarUseCase;
        this.desalocarUseCase = desalocarUseCase;
        this.iniciarManutencaoUseCase = iniciarManutencaoUseCase;
        this.finalizarManutencaoUseCase = finalizarManutencaoUseCase;
        this.cancelarAlocacaoUseCase = cancelarAlocacaoUseCase;
        this.listAlocacoesPorAtivoUseCase = listAlocacoesPorAtivoUseCase;
        this.listAlocacoesPorEventoUseCase = listAlocacoesPorEventoUseCase;
        this.metricasOciosidadeUseCase = metricasOciosidadeUseCase;
    }

    /**
     * Cadastra um novo ativo patrimonial, sempre iniciando no status {@code DISPONIVEL} e vinculado
     * a uma organização dona.
     *
     * @param request dados de cadastro do ativo
     * @return o ativo recém-criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Cadastra um novo ativo patrimonial (status inicial: DISPONIVEL)")
    public AtivoPatrimonialResponse create(@RequestBody @Valid CreateAtivoPatrimonialRequest request) {
        return createUseCase.execute(request);
    }

    /**
     * Lista o inventário completo de ativos patrimoniais pertencentes a uma organização.
     *
     * @param grupoParoquialId organização dona dos ativos
     * @return ativos cadastrados para a organização
     */
    @GetMapping("/grupoParoquial/{grupoParoquialId}")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Lista o inventário de ativos patrimoniais de uma organização")
    public List<AtivoPatrimonialResponse> listByGrupoParoquial(@PathVariable UUID grupoParoquialId) {
        return listUseCase.execute(grupoParoquialId);
    }

    /**
     * Transfere a posse de um ativo patrimonial para outra organização, sujeito às regras de
     * {@code AtivoGrupoParoquialPolicy} (ex.: bloqueio de transferência com alocação ativa).
     *
     * @param id      ativo a transferir
     * @param request organização de destino
     * @return o ativo com a organização dona atualizada
     */
    @PatchMapping("/{id}/transferir")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Transfere um ativo patrimonial para outra organização")
    public AtivoPatrimonialResponse transferir(@PathVariable UUID id,
                                                @RequestBody @Valid TransferirAtivoRequest request) {
        return transferirUseCase.execute(id, request);
    }

    /**
     * Aloca um ativo patrimonial a um evento, exigindo o registro da nota de uso. Quando a
     * organização do evento diverge da organização dona do ativo, a alocação não é aplicada
     * de imediato: fica pendente de aprovação, caracterizando um empréstimo entre organizações
     * (mesmo fluxo de aprovação usado em outras mutações sensíveis do sistema).
     *
     * @param id      ativo a alocar
     * @param request evento de destino, período e nota de uso
     * @return o resultado da operação, com status HTTP variável conforme o ativo seja alocado
     *     imediatamente ou fique pendente de aprovação
     */
    @PostMapping("/{id}/alocar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Aloca um ativo patrimonial a um evento, registrando a nota de uso obrigatória. "
            + "Se a organização do evento divergir da organização dona do ativo, o pedido fica pendente de "
            + "aprovação (empréstimo).")
    public ResponseEntity<Object> alocar(@PathVariable UUID id, @RequestBody @Valid AlocarAtivoRequest request) {
        AtivoOperationResult result = alocarUseCase.execute(id, request);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    /**
     * Desaloca um ativo patrimonial, encerrando a ocupação corrente e devolvendo-o para a
     * organização dona.
     *
     * @param id      ativo a desalocar
     * @param request motivo/observações da desalocação (opcional)
     * @return o ativo já disponível para a organização dona
     */
    @PatchMapping("/{id}/desalocar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Desaloca um ativo patrimonial, devolvendo-o para a organização dona")
    public AtivoPatrimonialResponse desalocar(@PathVariable UUID id,
                                               @RequestBody(required = false) @Valid DesalocarAtivoRequest request) {
        return desalocarUseCase.execute(id, request);
    }

    /**
     * Inicia um período de manutenção de um ativo patrimonial, retirando-o temporariamente de
     * circulação (não pode ser alocado a eventos enquanto em manutenção).
     *
     * @param id      ativo a colocar em manutenção
     * @param request motivo/período previsto da manutenção
     * @return o ativo com o status de manutenção aplicado
     */
    @PostMapping("/{id}/manutencao")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Inicia um período de manutenção de um ativo patrimonial")
    public AtivoPatrimonialResponse iniciarManutencao(@PathVariable UUID id,
                                                       @RequestBody @Valid IniciarManutencaoRequest request) {
        return iniciarManutencaoUseCase.execute(id, request);
    }

    /**
     * Finaliza a manutenção corrente de um ativo patrimonial, retornando-o ao status
     * {@code DISPONIVEL}.
     *
     * @param id ativo em manutenção
     * @return o ativo já disponível novamente
     */
    @PatchMapping("/{id}/manutencao/finalizar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Finaliza a manutenção corrente de um ativo, devolvendo-o para DISPONIVEL")
    public AtivoPatrimonialResponse finalizarManutencao(@PathVariable UUID id) {
        return finalizarManutencaoUseCase.execute(id);
    }

    /**
     * Cancela uma reserva/pedido de alocação de ativo que ainda não teve seu período iniciado,
     * incluindo pedidos pendentes de aprovação de empréstimo entre organizações.
     *
     * @param alocacaoId alocação a cancelar
     * @param request    motivo do cancelamento
     */
    @PatchMapping("/alocacoes/{alocacaoId}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Cancela uma reserva/pedido de alocação de ativo ainda não iniciado")
    public void cancelarAlocacao(@PathVariable UUID alocacaoId,
                                  @RequestBody @Valid CancelarAlocacaoRequest request) {
        cancelarAlocacaoUseCase.execute(alocacaoId, request);
    }

    /**
     * Lista o histórico completo de ocupações de um ativo patrimonial, incluindo alocações a
     * eventos e períodos de manutenção.
     *
     * @param id ativo consultado
     * @return histórico de ocupações do ativo
     */
    @GetMapping("/{id}/alocacoes")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Lista o histórico de ocupações (alocações e manutenções) de um ativo patrimonial")
    public List<AtivoAlocacaoResponse> listAlocacoesPorAtivo(@PathVariable UUID id) {
        return listAlocacoesPorAtivoUseCase.execute(id);
    }

    /**
     * Lista todos os ativos patrimoniais alocados a um evento específico.
     *
     * @param eventoId evento consultado
     * @return ativos alocados ao evento
     */
    @GetMapping("/evento/{eventoId}/alocacoes")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Lista os ativos patrimoniais alocados a um evento")
    public List<AtivoAlocacaoResponse> listAlocacoesPorEvento(@PathVariable UUID eventoId) {
        return listAlocacoesPorEventoUseCase.execute(eventoId);
    }

    /**
     * Calcula métricas agregadas de ociosidade e taxa de alocação dos ativos patrimoniais, usadas
     * para identificar bens subutilizados no acervo da paróquia.
     *
     * @return métricas de ociosidade/alocação agregadas
     */
    @GetMapping("/metricas/ociosidade")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Métricas de ociosidade/alocação de ativos patrimoniais")
    public MetricasOciosidadeResponse metricasOciosidade() {
        return metricasOciosidadeUseCase.execute();
    }
}
