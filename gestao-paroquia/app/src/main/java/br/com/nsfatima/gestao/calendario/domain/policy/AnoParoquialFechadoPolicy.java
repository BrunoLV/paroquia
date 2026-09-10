package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.CalendarioBloqueadoException;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;

import java.time.Instant;
import java.time.ZoneOffset;

/** Permite exceção extraordinária, mas bloqueia alterações ordinárias em ano paroquial fechado. */
public final class AnoParoquialFechadoPolicy implements BloqueioAnoParoquialPolicy {

    @Override
    public void assertPermitido(Instant dataEvento, AnoParoquialStatus statusAno, EventoStatusInput statusEvento) {
        if (dataEvento != null && statusAno == AnoParoquialStatus.FECHADO
                && statusEvento != EventoStatusInput.ADICIONADO_EXTRA) {
            throw new CalendarioBloqueadoException(dataEvento.atZone(ZoneOffset.UTC).getYear());
        }
    }
}
