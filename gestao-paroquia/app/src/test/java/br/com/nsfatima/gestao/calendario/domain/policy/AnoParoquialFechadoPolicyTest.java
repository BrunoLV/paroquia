package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.CalendarioBloqueadoException;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Especifica a exceção extraordinária admitida após o fechamento do ano paroquial. */
@DisplayName("Policy: bloqueio de ano paroquial")
class AnoParoquialFechadoPolicyTest {

    private final AnoParoquialFechadoPolicy policy = new AnoParoquialFechadoPolicy();

    /** RN-CAL-01: evento ordinário não altera calendário de ano fechado. */
    @Test
    @DisplayName("bloqueia evento ordinário em ano fechado")
    void bloqueiaEventoOrdinarioEmAnoFechado() {
        assertThatThrownBy(() -> policy.assertPermitido(
                Instant.parse("2026-04-12T10:00:00Z"), AnoParoquialStatus.FECHADO, EventoStatusInput.CONFIRMADO))
                .isInstanceOf(CalendarioBloqueadoException.class);
    }

    /** RN-CAL-01: evento extraordinário é a única exceção ao bloqueio anual. */
    @Test
    @DisplayName("permite evento extraordinário em ano fechado")
    void permiteEventoExtraordinarioEmAnoFechado() {
        assertThatCode(() -> policy.assertPermitido(
                Instant.parse("2026-04-12T10:00:00Z"), AnoParoquialStatus.FECHADO,
                EventoStatusInput.ADICIONADO_EXTRA)).doesNotThrowAnyException();
    }
}
