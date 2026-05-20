package br.com.nsfatima.gestao.aprovacao.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoResponse;

@Schema(description = "Dados detalhados de uma solicitação de aprovação")
public record AprovacaoResponse(
        @Schema(description = "ID único da aprovação")
        UUID id,

        @Schema(description = "ID do evento associado")
        UUID eventoId,

        @Schema(description = "Tipo da solicitação")
        TipoSolicitacaoResponse tipoSolicitacao,

        @Schema(description = "Status atual da solicitação")
        String status,

        @Schema(description = "Papel do aprovador responsável")
        String aprovadorPapel,

        @Schema(description = "Data e hora de criação (UTC)")
        Instant criadoEmUtc,

        @Schema(description = "Data e hora da decisão (UTC)")
        Instant decididoEmUtc,

        @Schema(description = "ID do solicitante")
        String solicitanteId,

        @Schema(description = "ID do aprovador (se decidido)")
        String aprovadorId,

        @Schema(description = "Mensagem de erro em caso de falha na execução automática")
        String mensagemErroExecucao) {
}
