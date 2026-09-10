package br.com.nsfatima.gestao.support.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Raiz de Agregado.
 *
 * <p>Fronteira transacional e de consistência do modelo de domínio rico. Estende esta classe
 * toda entidade que é ponto de entrada de um agregado; o estado interno só muda por métodos
 * da Linguagem Ubíqua (nunca setters públicos), e cada mutação relevante registra um
 * {@link DomainEvent} via {@link #registrarEvento(DomainEvent)}.</p>
 *
 * <p>A camada de aplicação, após persistir a raiz, drena os eventos com
 * {@link #extrairEventos()} e os publica (ver {@code PublicadorEventosDominio}).</p>
 *
 * @param <ID> tipo do identificador tipado do agregado
 */
public abstract class AggregateRoot<ID> {

    private final transient List<DomainEvent> eventosDominio = new ArrayList<>();

    protected void registrarEvento(DomainEvent evento) {
        this.eventosDominio.add(evento);
    }

    /**
     * Devolve os eventos acumulados e limpa o buffer interno. Idempotente após a primeira chamada.
     */
    public List<DomainEvent> extrairEventos() {
        List<DomainEvent> copia = List.copyOf(this.eventosDominio);
        this.eventosDominio.clear();
        return copia;
    }

    public abstract ID id();
}
