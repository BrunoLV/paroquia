package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.util.UUID;

public class FakeEventoActorContextResolver extends EventoActorContextResolver {

    private EventoActorContext context;

    public FakeEventoActorContextResolver() {
        super(null);
        this.context = new EventoActorContext(
                "fake-actor",
                "tester",
                "CONSELHO",
                UUID.randomUUID(),
                UUID.randomUUID());
    }

    public void setContext(EventoActorContext context) {
        this.context = context;
    }

    @Override
    public EventoActorContext resolveRequired() {
        return context;
    }
}
