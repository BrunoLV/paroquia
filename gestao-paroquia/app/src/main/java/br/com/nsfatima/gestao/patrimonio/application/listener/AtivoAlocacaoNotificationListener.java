package br.com.nsfatima.gestao.patrimonio.application.listener;

import br.com.nsfatima.gestao.patrimonio.domain.event.AlocacaoAtivoPendenteEvent;
import br.com.nsfatima.gestao.patrimonio.domain.event.AtivoAlocacaoAtrasoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AtivoAlocacaoNotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivoAlocacaoNotificationListener.class);

    @Async
    @EventListener
    public void handleAtraso(AtivoAlocacaoAtrasoEvent event) {
        LOGGER.info(
                "NOTIFICAÇÃO: Ocupação {} do ativo {} ({}) está atrasada. Espaço para futuras integrações "
                        + "(E-mail, Push, etc.)",
                event.alocacaoId(), event.ativoId(), event.tipo());
    }

    @Async
    @EventListener
    public void handlePendenteDeAprovacao(AlocacaoAtivoPendenteEvent event) {
        LOGGER.info(
                "NOTIFICAÇÃO: Solicitação de empréstimo {} do ativo {} para o evento {} está pendente de aprovação. "
                        + "Solicitante: {}. Espaço para futuras integrações (E-mail, Push, etc.)",
                event.aprovacaoId(), event.ativoId(), event.eventoId(), event.solicitanteId());
    }
}
