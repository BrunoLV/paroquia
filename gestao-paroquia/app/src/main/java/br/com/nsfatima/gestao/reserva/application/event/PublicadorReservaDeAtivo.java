package br.com.nsfatima.gestao.reserva.application.event;

import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Centraliza a publicação de {@link ReservaDeAtivoAlteradaEvent} a partir das transições de estado
 * de reservas, evitando repetir a checagem de {@code tipoItem == ATIVO} em cada caso de uso.
 */
@Component
public class PublicadorReservaDeAtivo {

    private final ApplicationEventPublisher eventPublisher;

    public PublicadorReservaDeAtivo(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /** Publica o evento se a reserva for de um ativo patrimonial; caso contrário, no-op. */
    public void publicarSeForAtivo(Reserva reserva) {
        if (reserva != null && reserva.getTipoItem() == TipoItemReserva.ATIVO) {
            eventPublisher.publishEvent(new ReservaDeAtivoAlteradaEvent(reserva.getItemId()));
        }
    }

    /** Publica o evento diretamente para o ativo informado. */
    public void publicarParaAtivo(UUID ativoId) {
        if (ativoId != null) {
            eventPublisher.publishEvent(new ReservaDeAtivoAlteradaEvent(ativoId));
        }
    }
}
