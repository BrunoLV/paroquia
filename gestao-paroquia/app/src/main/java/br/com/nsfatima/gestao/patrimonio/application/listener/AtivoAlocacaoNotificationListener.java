package br.com.nsfatima.gestao.patrimonio.application.listener;

import br.com.nsfatima.gestao.patrimonio.domain.event.AlocacaoAtivoPendenteEvent;
import br.com.nsfatima.gestao.patrimonio.domain.event.AtivoAlocacaoAtrasoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reage assincronamente a eventos de domínio do contexto de patrimônio (atraso de ocupação,
 * empréstimo pendente de aprovação) para disparar notificações. Hoje apenas registra em log; é o
 * ponto de extensão previsto para integrações futuras (e-mail, push).
 */
@Component
public class AtivoAlocacaoNotificationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivoAlocacaoNotificationListener.class);

    /**
     * Notifica que uma ocupação (alocação ou manutenção) de ativo ultrapassou sua data de fim
     * sem ser devolvida.
     *
     * @param event evento de atraso, com a alocação e o ativo afetados
     */
    @Async
    @EventListener
    public void handleAtraso(AtivoAlocacaoAtrasoEvent event) {
        LOGGER.info(
                "NOTIFICAÇÃO: Ocupação {} do ativo {} ({}) está atrasada. Espaço para futuras integrações "
                        + "(E-mail, Push, etc.)",
                event.alocacaoId(), event.ativoId(), event.tipo());
    }

    /**
     * Notifica que um pedido de empréstimo de ativo entre organizações ficou pendente de
     * aprovação, para alertar o solicitante/aprovador.
     *
     * @param event evento de pendência, com a aprovação, o ativo, o evento e o solicitante
     */
    @Async
    @EventListener
    public void handlePendenteDeAprovacao(AlocacaoAtivoPendenteEvent event) {
        LOGGER.info(
                "NOTIFICAÇÃO: Solicitação de empréstimo {} do ativo {} para o evento {} está pendente de aprovação. "
                        + "Solicitante: {}. Espaço para futuras integrações (E-mail, Push, etc.)",
                event.aprovacaoId(), event.ativoId(), event.eventoId(), event.solicitanteId());
    }
}
