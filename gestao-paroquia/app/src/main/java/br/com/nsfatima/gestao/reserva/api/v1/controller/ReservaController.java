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
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNotFoundException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import br.com.nsfatima.gestao.reserva.infrastructure.security.ReservaSecurityAdapter;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

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
    public List<ReservaResponse> listar(
            @RequestParam(required = false) TipoItemReserva tipoItem,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) StatusReserva status,
            @RequestParam(required = false) UUID pastoralMovimentoId,
            @RequestParam(required = false) UUID solicitanteId,
            @RequestParam(required = false) UUID eventoId,
            @RequestParam(required = false) Instant dataInicio,
            @RequestParam(required = false) Instant dataFim,
            @RequestParam(required = false) String termoBusca
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
    public ReservaResponse buscarPorId(@PathVariable UUID id) {
        ReservaDetalheDto dto = buscarReservaPorIdUseCase.executar(id);
        return ReservaResponse.de(dto);
    }

    @PostMapping("/{id}/aprovar")
    public ReservaResponse aprovar(@PathVariable UUID id) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva aprovada = aprovarReservaUseCase.executar(id, ator);
        return ReservaResponse.de(enricherService.enriquecer(aprovada));
    }

    @PostMapping("/{id}/rejeitar")
    public ReservaResponse rejeitar(@PathVariable UUID id, @RequestBody @Valid RejeitarReservaRequest request) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva rejeitada = rejeitarReservaUseCase.executar(id, request.motivo(), ator);
        return ReservaResponse.de(enricherService.enriquecer(rejeitada));
    }

    @PostMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable UUID id, @RequestBody @Valid CancelarReservaRequest request) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva cancelada = cancelarReservaUseCase.executar(id, request.motivo(), request.itemNome(), ator);
        return ReservaResponse.de(enricherService.enriquecer(cancelada));
    }

    @PostMapping("/{id}/retirar")
    public ReservaResponse registrarRetirada(@PathVariable UUID id) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva retirada = registrarRetiradaAtivoUseCase.executar(id, ator);
        return ReservaResponse.de(enricherService.enriquecer(retirada));
    }

    @PostMapping("/{id}/devolver")
    public ReservaResponse registrarDevolucao(@PathVariable UUID id, @RequestBody @Valid RegistrarDevolucaoRequest request) {
        UsuarioActor ator = securityAdapter.obterAtorAutenticado();
        Reserva devolvida = registrarDevolucaoAtivoUseCase.executar(id, request.checkAvarias(), ator);
        return ReservaResponse.de(enricherService.enriquecer(devolvida));
    }

    @GetMapping("/disponibilidade")
    public List<DisponibilidadeFaixaResponse> obterDisponibilidade(
            @RequestParam TipoItemReserva tipoItem,
            @RequestParam UUID itemId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        List<DisponibilidadeFaixaDto> faixas = obterDisponibilidadeItemUseCase.executar(tipoItem, itemId, data);
        return faixas.stream().map(DisponibilidadeFaixaResponse::de).toList();
    }

    @GetMapping("/metricas")
    public MetricasReservasResponse obterMetricas(@RequestParam(required = false) UUID pastoralId) {
        MetricasReservasDto dto = obterMetricasReservasUseCase.executar(pastoralId);
        return MetricasReservasResponse.de(dto);
    }

    @ExceptionHandler(ReservaConflitoException.class)
    public ResponseEntity<Map<String, Object>> handleConflito(ReservaConflitoException ex) {
        List<ConflitoDetalheResponse> detalhes = ex.getConflitos().stream()
                .map(ConflitoDetalheResponse::de)
                .toList();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "error", "CONFLITO_RESERVA",
                "message", ex.getMessage(),
                "conflitos", detalhes
        ));
    }

    @ExceptionHandler(ReservaNaoAutorizadaException.class)
    public ResponseEntity<Map<String, String>> handleNaoAutorizada(ReservaNaoAutorizadaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "ACESSO_NEGADO",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ReservaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ReservaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "NAO_ENCONTRADO",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, ReservaOperacaoInvalidaException.class})
    public ResponseEntity<Map<String, String>> handleBadRequests(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "REQUISICAO_INVALIDA",
                "message", ex.getMessage()
        ));
    }
}
