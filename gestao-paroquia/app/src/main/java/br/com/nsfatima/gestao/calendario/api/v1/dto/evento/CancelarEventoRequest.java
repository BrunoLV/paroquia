package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo da requisição do endpoint "resiliente" de cancelamento de evento
 * ({@code EventoController#cancelResilient}), internamente convertido para {@link CancelEventoRequest}
 * antes de ser repassado ao use case. O motivo é obrigatório para compor o registro de auditoria e,
 * quando o evento já foi aprovado, é exibido ao aprovador na solicitação de cancelamento pendente.
 */
public record CancelarEventoRequest(
        @NotBlank @Size(max = 2000) String motivo) {
}
