package br.com.nsfatima.gestao.observabilidade.api.v1.controller;

import br.com.nsfatima.gestao.observabilidade.api.v1.dto.LoteLogClienteRequest;
import br.com.nsfatima.gestao.observabilidade.api.v1.dto.LogClienteDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
 * permitindo que sejam indexados pelo Loki e inspecionados em tempo real no Grafana. Exige sessão
 * autenticada (endpoint anônimo permitiria inundar e forjar logs); o conteúdo é higienizado pelo
 * {@link LogClienteDTO} (PII, caracteres de controle, tamanhos) e o lote é limitado a 50 eventos.
 */
@RestController
@RequestMapping("/api/v1/observabilidade/logs")
@Tag(name = "Observabilidade", description = "Endpoints de telemetria, rastreamento e ingestão de logs da aplicação")
public class LogClienteController {

    private static final Logger LOGGER = LoggerFactory.getLogger("br.com.nsfatima.gestao.observabilidade.ui");

    private static final String LOG_FORMAT_STANDARD =
            "[UI_CLIENT] corrId={} user={} route={} component={} msg={}";

    /**
     * Recebe um lote de logs emitidos pela aplicação cliente (React).
     *
     * @param request lote contendo os eventos de log da UI
     * @return 202 Accepted indicando recepção do lote
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Registrar lote de logs do frontend",
            description = "Recebe eventos de erro e logs de interface gerados pela SPA React e os "
                    + "encaminha para a esteira do Log4j2/Loki."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Lote de logs aceito para processamento assíncrono"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload inválido ou lote vazio",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<Void> registrarLogs(@Valid @RequestBody LoteLogClienteRequest request) {
        String usuarioAtor = resolverUsuarioAutenticado();

        for (LogClienteDTO log : request.logs()) {
            despacharLog(log, usuarioAtor);
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private void despacharLog(LogClienteDTO log, String usuarioAtor) {
        String correlationId = MDC.get("correlationId");
        String idRastreio = correlationId != null ? correlationId : "N/A";

        switch (log.nivel()) {
            case "ERROR" -> LOGGER.error(
                    "[UI_CLIENT] corrId={} user={} route={} component={} msg={} details={}",
                    idRastreio, usuarioAtor, log.rota(), log.componente(), log.mensagem(), log.detalhes()
            );
            case "WARN" -> LOGGER.warn(
                    LOG_FORMAT_STANDARD,
                    idRastreio, usuarioAtor, log.rota(), log.componente(), log.mensagem()
            );
            case "DEBUG", "TRACE" -> LOGGER.debug(
                    LOG_FORMAT_STANDARD,
                    idRastreio, usuarioAtor, log.rota(), log.componente(), log.mensagem()
            );
            default -> LOGGER.info(
                    LOG_FORMAT_STANDARD,
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
