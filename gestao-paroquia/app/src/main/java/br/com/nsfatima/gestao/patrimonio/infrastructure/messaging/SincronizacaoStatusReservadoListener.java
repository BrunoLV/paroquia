package br.com.nsfatima.gestao.patrimonio.infrastructure.messaging;

import br.com.nsfatima.gestao.patrimonio.application.usecase.SincronizarStatusReservadoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.event.ManutencaoAtivoConcluidaEvent;
import br.com.nsfatima.gestao.reserva.application.event.ReservaDeAtivoAlteradaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

/**
 * Recalcula o status {@code RESERVADO}/{@code DISPONIVEL} de um ativo imediatamente após qualquer
 * transição de uma reserva de ativo ({@link ReservaDeAtivoAlteradaEvent}) ou após a conclusão de
 * uma manutenção ({@link ManutencaoAtivoConcluidaEvent}). Roda após o commit da transação de
 * origem e de forma assíncrona; o job de varredura ({@code SincronizarStatusReservadoJob}) é o
 * backstop para os casos sem evento (início/fim de período de reserva).
 */
@Component
public class SincronizacaoStatusReservadoListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SincronizacaoStatusReservadoListener.class);

    private final SincronizarStatusReservadoAtivoUseCase sincronizarUseCase;

    public SincronizacaoStatusReservadoListener(SincronizarStatusReservadoAtivoUseCase sincronizarUseCase) {
        this.sincronizarUseCase = sincronizarUseCase;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAlterarReservaDeAtivo(ReservaDeAtivoAlteradaEvent evento) {
        sincronizar(evento.ativoId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoConcluirManutencao(ManutencaoAtivoConcluidaEvent evento) {
        sincronizar(evento.ativoId());
    }

    private void sincronizar(UUID ativoId) {
        try {
            sincronizarUseCase.execute(ativoId);
        } catch (RuntimeException ex) {
            LOGGER.warn("Falha ao sincronizar status RESERVADO do ativo {}: {}", ativoId, ex.getMessage());
        }
    }
}
