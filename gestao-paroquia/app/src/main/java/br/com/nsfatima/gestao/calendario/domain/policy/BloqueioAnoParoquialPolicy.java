package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;

import java.time.Instant;

/** Define a decisão que protege um calendário vinculado a ano paroquial fechado. */
@FunctionalInterface
public interface BloqueioAnoParoquialPolicy {

    /**
     * Garante que o evento seja admissível diante do status do ano já carregado pela aplicação.
     *
     * @param dataEvento instante do evento
     * @param statusAno status do ano correspondente, ou {@code null} quando ele ainda não existe
     * @param statusEvento estado pretendido para o evento
     */
    void assertPermitido(Instant dataEvento, AnoParoquialStatus statusAno, EventoStatusInput statusEvento);
}
