package br.com.nsfatima.gestao.aprovacao.api.v1.dto;

import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Corpo da requisição para criar uma nova solicitação de aprovação (status inicial PENDENTE) para um evento.
 */
@Schema(description = "Solicitação para criação de uma nova aprovação de evento")
public record AprovacaoCreateRequest(
        @Schema(description = "ID do evento a ser aprovado")
        @NotNull UUID eventoId,

        @Schema(description = "Tipo da solicitação de aprovação")
        @NotNull TipoSolicitacaoInput tipoSolicitacao) {
}
