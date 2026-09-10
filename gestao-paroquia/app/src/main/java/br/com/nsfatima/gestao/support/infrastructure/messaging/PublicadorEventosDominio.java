package br.com.nsfatima.gestao.support.infrastructure.messaging;

import br.com.nsfatima.gestao.support.domain.AggregateRoot;
import br.com.nsfatima.gestao.support.domain.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Ponte entre a coleção interna de eventos de um {@link AggregateRoot} e o barramento
 * de eventos do Spring.
 *
 * <p>Convenção: todo caso de uso, após {@code repository.save(agregado)}, chama
 * {@link #publicar(AggregateRoot)}. Isso substitui chamadas diretas e espalhadas a
 * {@link ApplicationEventPublisher#publishEvent(Object)}.</p>
 */
@Component
public class PublicadorEventosDominio {

    private final ApplicationEventPublisher publisher;

    public PublicadorEventosDominio(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publicar(AggregateRoot<?> raiz) {
        for (DomainEvent evento : raiz.extrairEventos()) {
            publisher.publishEvent(evento);
        }
    }
}
