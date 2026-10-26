package br.com.nsfatima.gestao.observabilidade.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * DTO que representa um evento de log ou erro capturado no frontend (SPA).
 *
 * <p>Aplica validação no construtor compacto e higienização automática de PII (LGPD PII Zero),
 * garantindo que nenhum documento civil (CPF, RG), senha, e-mail ou cartão trafegue em claro
 * para a esteira de logs do Log4j2 e Loki.
 *
 * @param nivel severidade do log (INFO, WARN, ERROR, DEBUG)
 * @param mensagem descrição sucinta do evento ou falha
 * @param rota caminho da aplicação onde o evento ocorreu (ex: /fieis)
 * @param componente identificador do componente React emissor
 * @param detalhes rastreamento de pilha (stack trace) ou contexto técnico
 * @param timestamp momento da emissão no navegador cliente
 */
@Schema(description = "Registro de log ou falha emitido pelo cliente web")
public record LogClienteDTO(
        @NotBlank(message = "O nível do log é obrigatório")
        @Schema(description = "Severidade do log", example = "ERROR")
        String nivel,

        @NotBlank(message = "A mensagem de log é obrigatória")
        @Size(max = 1000, message = "A mensagem não pode exceder 1000 caracteres")
        @Schema(description = "Mensagem do evento ou erro", example = "Erro ao renderizar formulário de fiel")
        String mensagem,

        @Size(max = 255, message = "A rota não pode exceder 255 caracteres")
        @Schema(description = "Rota ativa no navegador", example = "/fieis")
        String rota,

        @Size(max = 255, message = "O nome do componente não pode exceder 255 caracteres")
        @Schema(description = "Componente emissor", example = "FielEditModal")
        String componente,

        @Size(max = 2000, message = "Os detalhes técnicos não podem exceder 2000 caracteres")
        @Schema(description = "Detalhes adicionais ou stack trace sanitizado")
        String detalhes,

        @Schema(description = "Timestamp UTC de geração no cliente")
        Instant timestamp
) {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");


    /**
     * Construtor compacto com validações e sanitização mandatória de PII.
     */
    public LogClienteDTO {
        nivel = (nivel == null || nivel.isBlank()) ? "INFO" : nivel.trim().toUpperCase();
        if (!nivel.matches("^(INFO|WARN|ERROR|DEBUG|TRACE)$")) {
            nivel = "INFO";
        }

        mensagem = sanitizarPii(neutralizarControles(mensagem != null ? mensagem.trim() : ""));
        if (mensagem.length() > 1000) {
            mensagem = mensagem.substring(0, 1000);
        }

        rota = rota != null ? neutralizarControles(rota.trim()) : "/";
        if (rota.length() > 255) {
            rota = rota.substring(0, 255);
        }

        componente = componente != null ? neutralizarControles(componente.trim()) : "App";
        if (componente.length() > 255) {
            componente = componente.substring(0, 255);
        }

        detalhes = detalhes != null ? sanitizarPii(neutralizarControles(detalhes.trim())) : null;
        if (detalhes != null && detalhes.length() > 2000) {
            detalhes = detalhes.substring(0, 2000);
        }

        timestamp = timestamp != null ? timestamp : Instant.now();
    }

    /**
     * Impede forja de entradas de log (log forging): quebras de linha viram marcadores literais
     * ({@code \\n}, {@code \\r}) preservando a legibilidade de stack traces, e demais caracteres de
     * controle (inclusive sequências ANSI) viram espaço. O corpo é controlado pelo cliente e é emitido
     * em formato de linha única consumido pelo Loki, portanto jamais pode introduzir novas linhas.
     */
    private static String neutralizarControles(String texto) {
        StringBuilder limpo = new StringBuilder(texto.length());
        texto.chars().forEach(c -> {
            if (c == '\n') {
                limpo.append("\\n");
            } else if (c == '\r') {
                limpo.append("\\r");
            } else if (Character.isISOControl(c)) {
                limpo.append(' ');
            } else {
                limpo.append((char) c);
            }
        });
        return limpo.toString();
    }

    /**
     * Remove ou mascara dados pessoais sensíveis conforme a LGPD antes de persistir ou registrar em logs.
     */
    private static String sanitizarPii(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }
        String sanitizado = CPF_PATTERN.matcher(texto).replaceAll("***.***.***-**");
        return EMAIL_PATTERN.matcher(sanitizado).replaceAll("*****@*****.***");
    }
}
