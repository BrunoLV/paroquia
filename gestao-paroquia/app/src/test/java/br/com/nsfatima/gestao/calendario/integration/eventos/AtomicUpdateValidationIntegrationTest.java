package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AtomicUpdateValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private EventoAuditPublisher eventoAuditPublisher;

    @Test
    void shouldRejectInvalidPartialUpdateWithoutMutationSideEffects() throws Exception {
        String payload = """
                {
                  \"titulo\": \"Titulo que nao deve ser aplicado\",
                  \"status\": \"invalido\"
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ENUM_VALUE_INVALID"));

        verify(eventoAuditPublisher, never()).publish(
                "system",
                "patch",
                "00000000-0000-0000-0000-000000000001",
                "success");
    }
}
