package br.com.nsfatima.gestao.reserva.api.v1.controller;

import br.com.nsfatima.gestao.reserva.api.v1.dto.CancelarReservaRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.ConflitoDetalheResponse;
import br.com.nsfatima.gestao.reserva.api.v1.dto.CriarReservaRecorrenteRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.CriarReservaRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.DisponibilidadeFaixaResponse;
import br.com.nsfatima.gestao.reserva.api.v1.dto.MetricasReservasResponse;
import br.com.nsfatima.gestao.reserva.api.v1.dto.RegistrarDevolucaoRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.RejeitarReservaRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.ReservaRecorrenteResponse;
import br.com.nsfatima.gestao.reserva.api.v1.dto.ReservaResponse;
import br.com.nsfatima.gestao.reserva.application.dto.DisponibilidadeFaixaDto;
import br.com.nsfatima.gestao.reserva.application.dto.MetricasReservasDto;
import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.application.usecase.AprovarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.BuscarReservaPorIdUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CancelarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.ComandoCriarRecorrente;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.ResultadoRecorrencia;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaUseCase.ComandoCriarReserva;
import br.com.nsfatima.gestao.reserva.application.usecase.ListarReservasUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.ObterDisponibilidadeItemUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.ObterMetricasReservasUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.RegistrarDevolucaoAtivoUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.RegistrarRetiradaAtivoUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.RejeitarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.ReservaEnricherService;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import br.com.nsfatima.gestao.reserva.infrastructure.security.ReservaSecurityAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST para gerenciamento centralizado de reservas de dependências físicas (salas, salões)
 * e ativos patrimoniais (instrumentos, veículos, projetores), com controle de concorrência e buffer.
 */
@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Endpoints para agendamento e alocação de locais físicos e ativos patrimoniais")
public class ReservaController {

    private static final String AUTHENTICATED = "isAuthenticated()";

    private final CriarReservaUseCase criarReservaUseCase;
    private final CriarReservaRecorrenteUseCase criarReservaRecorrenteUseCase;
    private final AprovarReservaUseCase aprovarReservaUseCase;
    private final RejeitarReservaUseCase rejeitarReservaUseCase;
    private final CancelarReservaUseCase cancelarReservaUseCase;
    private final RegistrarRetiradaAtivoUseCase registrarRetiradaAtivoUseCase;
    private final RegistrarDevolucaoAtivoUseCase registrarDevolucaoAtivoUseCase;
    private final ListarReservasUseCase listarReservasUseCase;
    private final BuscarReservaPorIdUseCase buscarReservaPorIdUseCase;
    private final ObterDisponibilidadeItemUseCase obterDisponibilidadeItemUseCase;
    private final ObterMetricasReservasUseCase obterMetricasReservasUseCase;
    private final ReservaEnricherService enricherService;
    private final ReservaSecurityAdapter securityAdapter;

    public ReservaController(
            CriarReservaUseCase criarReservaUseCase,
            CriarReservaRecorrenteUseCase criarReservaRecorrenteUseCase,
            AprovarReservaUseCase aprovarReservaUseCase,
            RejeitarReservaUseCase rejeitarReservaUseCase,
            CancelarReservaUseCase cancelarReservaUseCase,
            RegistrarRetiradaAtivoUseCase registrarRetiradaAtivoUseCase,
            RegistrarDevolucaoAtivoUseCase registrarDevolucaoAtivoUseCase,
            ListarReservasUseCase listarReservasUseCase,
            BuscarReservaPorIdUseCase buscarReservaPorIdUseCase,
            ObterDisponibilidadeItemUseCase obterDisponibilidadeItemUseCase,
            ObterMetricasReservasUseCase obterMetricasReservasUseCase,
            ReservaEnricherService enricherService,
            ReservaSecurityAdapter securityAdapter
    ) {
        this.criarReservaUseCase = criarReservaUseCase;
        this.criarReservaRecorrenteUseCase = criarReservaRecorrenteUseCase;
        this.aprovarReservaUseCase = aprovarReservaUseCase;
        this.rejeitarReservaUseCase = rejeitarReservaUseCase;
        this.cancelarReservaUseCase = cancelarReservaUseCase;
        this.registrarRetiradaAtivoUseCase = registrarRetiradaAtivoUseCase;
        this.registrarDevolucaoAtivoUseCase = registrarDevolucaoAtivoUseCase;
        this.listarReservasUseCase = listarReservasUseCase;
        this.buscarReservaPorIdUseCase = buscarReservaPorIdUseCase;
        this.obterDisponibilidadeItemUseCase = obterDisponibilidadeItemUseCase;
        this.obterMetricasReservasUseCase = obterMetricasReservasUseCase;
        this.enricherService = enricherService;
        this.securityAdapter = securityAdapter;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Solicita uma nova reserva pontual de local ou ativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva criada com sucesso", content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos ou violação de regras de validação"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o solicitante"),
            @ApiResponse(responseCode = "409", description = "Conflito de agenda ou tempo de carência (buffer)")
    })
    public ReservaResponse criar(@RequestBody @Valid CriarReservaRequest request) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();

        ComandoCriarReserva comando = new ComandoCriarReserva(
                request.tipoItem(),
                request.itemId(),
                request.itemNome(),
                request.dataHoraInicio(),
                request.dataHoraFim(),
                request.bufferMinutos(),
                request.descricao(),
                request.pastoralMovimentoId(),
                request.eventoId(),
                request.grupoRecorrenciaId()
        );

        Reserva criada = criarReservaUseCase.executar(comando, ator);
        ReservaDetalheDto enriquecida = enricherService.enriquecer(criada);
        return ReservaResponse.de(enriquecida);
    }

    @PostMapping("/recorrente")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Gera uma série de reservas recorrentes em lote com relatório de conflitos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Lote de reservas recorrentes processado", content = @Content(schema = @Schema(implementation = ReservaRecorrenteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos de recorrência"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o solicitante")
    })
    public ReservaRecorrenteResponse criarRecorrente(@RequestBody @Valid CriarReservaRecorrenteRequest request) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();

        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                request.tipoItem(),
                request.itemId(),
                request.itemNome(),
                request.bufferMinutos(),
                request.descricao(),
                request.pastoralMovimentoId(),
                request.ocorrencias(),
                request.salvarValidasDescartarConflitos()
        );

        ResultadoRecorrencia resultado = criarReservaRecorrenteUseCase.executar(comando, ator);
        List<ReservaDetalheDto> dtos = enricherService.enriquecer(resultado.reservasCriadas());
        List<ReservaResponse> salvas = dtos.stream().map(ReservaResponse::de).toList();
        List<ConflitoDetalheResponse> conflitos = resultado.conflitosIdentificados().stream()
                .map(ConflitoDetalheResponse::de)
                .toList();

        return new ReservaRecorrenteResponse(resultado.grupoRecorrenciaId(), salvas, conflitos);
    }

    @GetMapping
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Lista reservas existentes com filtros contextuais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas retornada com sucesso")
    })
    public List<ReservaResponse> listar(
            @Parameter(description = "Filtro por tipo de item (LOCAL ou ATIVO)") @RequestParam(required = false) TipoItemReserva tipoItem,
            @Parameter(description = "ID do item reservado") @RequestParam(required = false) UUID itemId,
            @Parameter(description = "Status da reserva") @RequestParam(required = false) StatusReserva status,
            @Parameter(description = "ID da pastoral ou movimento solicitante") @RequestParam(required = false) UUID pastoralMovimentoId,
            @Parameter(description = "ID do usuário solicitante") @RequestParam(required = false) UUID solicitanteId,
            @Parameter(description = "ID do evento associado") @RequestParam(required = false) UUID eventoId,
            @Parameter(description = "Data/hora inicial do intervalo de pesquisa") @RequestParam(required = false) Instant dataInicio,
            @Parameter(description = "Data/hora final do intervalo de pesquisa") @RequestParam(required = false) Instant dataFim,
            @Parameter(description = "Termo textual para busca em descrição ou nomes") @RequestParam(required = false) String termoBusca
    ) {
        ReservaFiltro filtro = new ReservaFiltro(
                tipoItem,
                itemId,
                status,
                pastoralMovimentoId,
                solicitanteId,
                eventoId,
                dataInicio,
                dataFim,
                termoBusca
        );

        List<ReservaDetalheDto> dtos = listarReservasUseCase.executar(filtro);
        return dtos.stream().map(ReservaResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Recupera os detalhes completos de uma reserva por identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes da reserva encontrados", content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    public ReservaResponse buscarPorId(@Parameter(description = "ID único da reserva") @PathVariable UUID id) {
        ReservaDetalheDto dto = buscarReservaPorIdUseCase.executar(id);
        return ReservaResponse.de(dto);
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Aprova uma solicitação de reserva pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva aprovada com sucesso", content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para aprovar esta reserva"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    public ReservaResponse aprovar(@Parameter(description = "ID único da reserva") @PathVariable UUID id) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva aprovada = aprovarReservaUseCase.executar(id, ator);
        return ReservaResponse.de(enricherService.enriquecer(aprovada));
    }

    @PatchMapping("/{id}/rejeitar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Rejeita uma solicitação de reserva pendente com justificativa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva rejeitada com sucesso", content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Motivo de rejeição não informado"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para rejeitar"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    public ReservaResponse rejeitar(
            @Parameter(description = "ID único da reserva") @PathVariable UUID id,
            @RequestBody @Valid RejeitarReservaRequest request
    ) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva rejeitada = rejeitarReservaUseCase.executar(id, request.motivo(), ator);
        return ReservaResponse.de(enricherService.enriquecer(rejeitada));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Cancela uma reserva confirmada ou pendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva cancelada com sucesso", content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Motivo não informado ou reserva já finalizada"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para cancelamento"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    public ReservaResponse cancelar(
            @Parameter(description = "ID único da reserva") @PathVariable UUID id,
            @RequestBody @Valid CancelarReservaRequest request
    ) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva cancelada = cancelarReservaUseCase.executar(id, request.motivo(), request.itemNome(), ator);
        return ReservaResponse.de(enricherService.enriquecer(cancelada));
    }

    @PatchMapping("/{id}/retirar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Registra a saída/retirada física de um bem móvel ou ativo patrimonial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retirada registrada com sucesso", content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Reserva não é do tipo ATIVO ou status não é CONFIRMADA"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    public ReservaResponse registrarRetirada(
            @Parameter(description = "ID único da reserva de ativo") @PathVariable UUID id) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva retirada = registrarRetiradaAtivoUseCase.executar(id, ator);
        return ReservaResponse.de(enricherService.enriquecer(retirada));
    }

    @PatchMapping("/{id}/devolver")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Registra a devolução física de um bem patrimonial com checklist obrigatório de avarias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolução registrada e reserva finalizada", content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Checklist de avarias ausente ou status incompatível"),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada")
    })
    public ReservaResponse registrarDevolucao(
            @Parameter(description = "ID único da reserva de ativo") @PathVariable UUID id,
            @RequestBody @Valid RegistrarDevolucaoRequest request
    ) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva devolvida = registrarDevolucaoAtivoUseCase.executar(id, request.checkAvarias(), ator);
        return ReservaResponse.de(enricherService.enriquecer(devolvida));
    }

    @GetMapping("/disponibilidade")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Consulta faixas e janelas horárias de disponibilidade para um local ou ativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de faixas ocupadas/livres retornada com sucesso")
    })
    public List<DisponibilidadeFaixaResponse> obterDisponibilidade(
            @Parameter(description = "Tipo do item (LOCAL ou ATIVO)") @RequestParam TipoItemReserva tipoItem,
            @Parameter(description = "ID do item a consultar") @RequestParam UUID itemId,
            @Parameter(description = "Data base da consulta no formato AAAA-MM-DD") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        List<DisponibilidadeFaixaDto> faixas = obterDisponibilidadeItemUseCase.executar(tipoItem, itemId, data);
        return faixas.stream().map(DisponibilidadeFaixaResponse::de).toList();
    }

    @GetMapping("/metricas")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Obtém métricas consolidadas de reservas para o painel de gestão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métricas retornadas com sucesso", content = @Content(schema = @Schema(implementation = MetricasReservasResponse.class)))
    })
    public MetricasReservasResponse obterMetricas(
            @Parameter(description = "Filtro opcional por ID da pastoral") @RequestParam(required = false) UUID pastoralId
    ) {
        MetricasReservasDto dto = obterMetricasReservasUseCase.executar(pastoralId);
        return MetricasReservasResponse.de(dto);
    }
}

