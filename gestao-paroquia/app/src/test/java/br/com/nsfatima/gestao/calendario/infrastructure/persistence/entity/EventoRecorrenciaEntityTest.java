package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventoRecorrenciaEntityTest {

    @Test
    void shouldMapRegraRecorrencia() {
        EventoRecorrenciaEntity entity = new EventoRecorrenciaEntity();
        RegraRecorrencia regra = new RegraRecorrencia(
                "SEMANAL",
                1,
                List.of(DayOfWeek.SUNDAY),
                null, null, null, null,
                null);

        entity.setRegra(regra);

        assertEquals(regra, entity.getRegra());
        assertEquals("SEMANAL", entity.getFrequencia());
        assertEquals(1, entity.getIntervalo());
    }
}
