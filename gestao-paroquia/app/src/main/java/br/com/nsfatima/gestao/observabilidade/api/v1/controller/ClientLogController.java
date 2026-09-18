package br.com.nsfatima.gestao.observabilidade.api.v1.controller;

import br.com.nsfatima.gestao.observabilidade.api.v1.dto.ClientLogBatchRequest;
import br.com.nsfatima.gestao.observabilidade.api.v1.dto.ClientLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST responsável por ingerir eventos de log, falhas de interface e telemetria da UI.
 *
 * <p>Recebe lotes de eventos do frontend (React / Vite), associa-os ao contexto de rastreabilidade
 * da sessão do usuário e os despacha para a esteira de logging assíncrono (Log4j2 / LMAX Disruptor),
 * permitindo que sejam indexados pelo Loki e inspecionados em tempo real no Grafana.
 */
@RestController
@RequestMapping("/api/v1/observabilidade/logs")
@Tag(name = "Observabilidade", description = "Endpoints de telemetria, rastreamento e ingestão de logs da aplicação")
public class ClientLogController {

    private static final Logger LOGGER = LoggerFactory.getLogger("br.com.nsfatima.gestao.observabilidade.ui");

    /**
     * Ingestão em lote de registros de log emitidos pelo cliente web.
     *
     * @param request payload contendo lote de registros de telemetria
     * @return resposta HTTP 202 (Accepted) confirmando o enfileiramento dos logs
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Registrar lote de logs do frontend",
            description = "Recebe eventos de erro e logs de interface gerados pela SPA React e os encaminha para a esteira do Log4j2/Loki."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Lote de logs aceito para processamento assíncrono"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido ou lote vazio",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<Void> registrarLogs(@Valid @RequestBody ClientLogBatchRequest request) {
        String usuarioAtor = resolverUsuarioAutenticado();

        for (ClientLogDto log : request.logs()) {
            despacharLog(log, usuarioAtor);
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private void despacharLog(ClientLogDto log, String usuarioAtor) {
        String correlationId = MDC.get("correlationId");
        String idRastreio = correlationId != null ? correlationId : "N/A";

        switch (log.nivel()) {
            case "ERROR" -> LOGGER.error(
                    "[UI_CLIENT] corrId={} user={} route={} component={} msg={} details={}",
                    idRastreio, usuarioAtor, log.rota(), log.componente(), log.mensagem(), log.detalhes()
            );
            case "WARN" -> LOGGER.warn(
                    "[UI_CLIENT] corrId={} user={} route={} component={} msg={}",
                    idRastreio, usuarioAtor, log.rota(), log.componente(), log.mensagem()
            );
            case "DEBUG", "TRACE" -> LOGGER.debug(
                    "[UI_CLIENT] corrId={} user={} route={} component={} msg={}",
                    idRastreio, usuarioAtor, log.rota(), log.componente(), log.mensagem()
            );
            default -> LOGGER.info(
                    "[UI_CLIENT] corrId={} user={} route={} component={} msg={}",
                    idRastreio, usuarioAtor, log.rota(), log.componente(), log.mensagem()
            );
        }
    }

    private String resolverUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "ANONYMOUS";
    }
}
