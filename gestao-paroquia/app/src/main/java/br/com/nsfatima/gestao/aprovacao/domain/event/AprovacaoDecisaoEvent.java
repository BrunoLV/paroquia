package br.com.nsfatima.gestao.aprovacao.domain.event;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;

import java.util.UUID;

/**
 * Evento de domínio publicado ao final do fluxo de decisão de uma aprovação (aprovada, reprovada ou com falha
 * de execução). Consumido de forma assíncrona por {@code AprovacaoNotificationListener} para notificar as
 * partes interessadas sem acoplar {@code DecideSolicitacaoAprovacaoUseCase} aos canais de notificação.
 *
 * @param aprovacaoId    identificador da solicitação de aprovação decidida
 * @param eventoId       identificador do evento de calendário associado à solicitação
 * @param solicitanteId  identificador do usuário que originou a solicitação
 * @param status         status final da aprovação (APROVADA, REPROVADA ou FALHA_EXECUCAO)
 * @param decisorId      identificador do usuário que tomou a decisão
 * @param erroExecucao   mensagem de erro quando a execução da ação aprovada falhou; {@code null} caso contrário
 */
public record AprovacaoDecisaoEvent(
        UUID aprovacaoId,
        UUID eventoId,
        String solicitanteId,
        AprovacaoStatus status,
        String decisorId,
        String erroExecucao) {
}
