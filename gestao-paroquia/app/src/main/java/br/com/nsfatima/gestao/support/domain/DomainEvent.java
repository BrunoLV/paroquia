package br.com.nsfatima.gestao.support.domain;

import java.time.Instant;

/**
 * Marcador de Evento de Domínio.
 *
 * <p>Todo evento de domínio é imutável, expressa um fato passado na Linguagem Ubíqua
 * (nome no particípio: {@code AprovacaoAprovada}, {@code AnoParoquialFechado}) e carrega
 * o instante em que o fato ocorreu. Coletado internamente pelo {@link AggregateRoot} e
 * despachado após a persistência da raiz.</p>
 */
public interface DomainEvent {

    Instant ocorridoEm();
}
