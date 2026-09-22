package br.com.nsfatima.gestao.aprovacao.api.v1.dto;

import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Representação detalhada de uma solicitação de aprovação para exibição/consulta via API, incluindo status
 * atual, dados de solicitante/aprovador e, em caso de falha de execução, a classificação da falha, mensagem
 * amigável e detalhes analíticos para troubleshooting.
 */
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
        String mensagemErroExecucao,

        @Schema(description = "Classificação da falha de execução (NEGOCIO_CADASTRAL ou SISTEMICA_APLICACAO)")
        String tipoFalhaExecucao,

        @Schema(description = "Detalhamento técnico e analítico da falha de execução para troubleshooting")
        String detalheErroExecucao,

        @Schema(description = "Payload detalhado em formato JSON com os dados do evento")
        String actionPayloadJson,

        @Schema(description = "ID da aprovação mestre caso seja ocorrência filha")
        UUID aprovacaoPaiId,

        @Schema(description = "JSON com detalhes dos conflitos detectados")
        String conflitosJson,

        @Schema(description = "Data e hora programada da ocorrência")
        Instant dataOcorrenciaUtc,

        @Schema(description = "Justificativa informada na submissão")
        String justificativa,

        @Schema(description = "Total de ocorrências filhas associadas a esta aprovação mestre")
        Integer totalOcorrenciasFilhas) {

    public AprovacaoResponse(
            UUID id,
            UUID eventoId,
            TipoSolicitacaoResponse tipoSolicitacao,
            String status,
            String aprovadorPapel,
            Instant criadoEmUtc,
            Instant decididoEmUtc,
            String solicitanteId,
            String aprovadorId,
            String mensagemErroExecucao,
            String actionPayloadJson,
            UUID aprovacaoPaiId,
            String conflitosJson,
            Instant dataOcorrenciaUtc,
            String justificativa,
            Integer totalOcorrenciasFilhas) {
        this(id, eventoId, tipoSolicitacao, status, aprovadorPapel, criadoEmUtc, decididoEmUtc,
                solicitanteId, aprovadorId, mensagemErroExecucao, null, null, actionPayloadJson,
                aprovacaoPaiId, conflitosJson, dataOcorrenciaUtc, justificativa, totalOcorrenciasFilhas);
    }

    public AprovacaoResponse(
            UUID id,
            UUID eventoId,
            TipoSolicitacaoResponse tipoSolicitacao,
            String status,
            String aprovadorPapel,
            Instant criadoEmUtc,
            Instant decididoEmUtc,
            String solicitanteId,
            String aprovadorId,
            String mensagemErroExecucao,
            String actionPayloadJson) {
        this(id, eventoId, tipoSolicitacao, status, aprovadorPapel, criadoEmUtc, decididoEmUtc,
                solicitanteId, aprovadorId, mensagemErroExecucao, null, null, actionPayloadJson,
                null, null, null, null, null);
    }
}
