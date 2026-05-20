package br.com.nsfatima.gestao.aprovacao.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ApprovalNotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalNotificationListener.class);

    @Async
    @EventListener
    public void handleApprovalDecision(ApprovalDecisionEvent event) {
        LOGGER.info("NOTIFICAÇÃO: Solicitação de aprovação {} para o evento {} foi {}. Solicitante: {}. Decisor: {}. Erro: {}",
                event.aprovacaoId(),
                event.eventoId(),
                event.status(),
                event.solicitanteId(),
                event.decisorId(),
                event.erroExecucao() != null ? event.erroExecucao() : "Nenhum");
        
        // Espaço para futuras integrações (E-mail, Push, etc.)
    }
}
