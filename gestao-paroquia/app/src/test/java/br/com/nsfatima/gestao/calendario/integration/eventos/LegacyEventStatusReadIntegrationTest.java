package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LegacyEventStatusReadIntegrationTest {

    @Test
    void deveExporDesconhecidoLegadoParaInvalidoArmazenadoStatus() {
        LegacyEnumInconsistencyPublisher publisher = Mockito.mock(LegacyEnumInconsistencyPublisher.class);

        EventoStatusResponse status = EventoStatusResponse.fromStoredValue(
                "status_antigo",
                publisher,
                "00000000-0000-0000-0000-000000000001");

        assertEquals(EventoStatusResponse.UNKNOWN_LEGACY, status);
        Mockito.verify(publisher).publish(
                "evento",
                "00000000-0000-0000-0000-000000000001",
                "status",
                "status_antigo");
    }
}
