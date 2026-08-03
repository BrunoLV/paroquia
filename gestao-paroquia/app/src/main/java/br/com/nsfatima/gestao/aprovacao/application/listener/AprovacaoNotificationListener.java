package br.com.nsfatima.gestao.aprovacao.application.listener;

import br.com.nsfatima.gestao.aprovacao.domain.event.AprovacaoDecisaoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Ouvinte assíncrono de {@code AprovacaoDecisaoEvent}, responsável por notificar as partes interessadas sobre
 * o desfecho de uma decisão de aprovação (aprovada, reprovada ou com falha de execução). Executa fora da
 * transação do {@code DecideSolicitacaoAprovacaoUseCase} (via {@code @Async}) para não impactar a latência
 * nem a atomicidade da decisão. Atualmente apenas registra em log; é o ponto de extensão previsto para
 * futuras integrações (e-mail, push, etc.).
 */
@Component
public class AprovacaoNotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AprovacaoNotificationListener.class);

    /**
     * Trata o evento de decisão de aprovação, registrando os dados relevantes (solicitação, evento, status,
     * solicitante, decisor e eventual erro de execução) para fins de notificação/observabilidade.
     *
     * @param event evento publicado ao final do fluxo de decisão de uma aprovação
     */
    @Async
    @EventListener
    public void handleAprovacaoDecision(AprovacaoDecisaoEvent event) {
        LOGGER.info(
                "NOTIFICAÇÃO: Solicitação de aprovação {} para o evento {} foi {}. Solicitante: {}. Decisor: {}. " +
                        "Erro: {}",
                event.aprovacaoId(),
                event.eventoId(),
                event.status(),
                event.solicitanteId(),
                event.decisorId(),
                event.erroExecucao() != null ? event.erroExecucao() : "Nenhum");

        // Espaço para futuras integrações (E-mail, Push, etc.)
    }
}
