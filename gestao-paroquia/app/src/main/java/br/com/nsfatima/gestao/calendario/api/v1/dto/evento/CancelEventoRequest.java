package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo da requisição de cancelamento padrão de um evento ({@code EventoController#cancel}). O
 * motivo é obrigatório para compor o registro de auditoria e, quando o evento já foi aprovado, é
 * exibido ao aprovador na solicitação de cancelamento pendente gerada pelo fluxo de aprovação.
 */
public record CancelEventoRequest(
        @NotBlank @Size(max = 2000) String motivo) {
}
