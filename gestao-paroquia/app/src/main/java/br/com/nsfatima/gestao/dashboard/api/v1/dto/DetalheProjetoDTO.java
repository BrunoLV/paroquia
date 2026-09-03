package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Progresso e risco de um projeto ativo (status {@code ABERTO} ou {@code EM_ANDAMENTO}) para
 * exibição no dashboard. {@code percentualTempoDecorrido} e {@code emRisco} vêm de
 * {@code ProjetoAgregacaoDomainService} (janela do projeto × pendências); {@code percentualExecucao}
 * é a razão eventos concluídos / eventos previstos, que pode divergir do tempo decorrido e
 * evidencia atraso de execução.
 *
 * @param nome nome do projeto
 * @param status status atual
 * @param percentualTempoDecorrido percentual (0-100) da janela do projeto já decorrida
 * @param totalEventos eventos vinculados ao projeto
 * @param eventosRealizados eventos que já ocorreram
 * @param percentualExecucao percentual (0-100) de eventos concluídos sobre os previstos
 * @param eventosPendentesAprovacao eventos do projeto ainda aguardando aprovação
 * @param emRisco prazo apertado com pendências
 * @param proximoMarco próximo evento crítico do projeto, ou {@code null}
 * @param envolvidos nomes das pastorais envolvidas
 */
@Schema(description = "Detalhes de um projeto ativo para exibição no dashboard")
public record DetalheProjetoDTO(
        String nome,
        String status,
        double percentualTempoDecorrido,
        int totalEventos,
        int eventosRealizados,
        double percentualExecucao,
        int eventosPendentesAprovacao,
        boolean emRisco,
        ProximoMarcoDTO proximoMarco,
        List<String> envolvidos
) {
}
