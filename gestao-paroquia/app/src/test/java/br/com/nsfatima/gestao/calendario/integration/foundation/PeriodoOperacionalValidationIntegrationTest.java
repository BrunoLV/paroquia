package br.com.nsfatima.gestao.calendario.integration.foundation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PeriodoOperacionalValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectAmbiguousOperationalPeriod() throws Exception {
        mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                .param("organizacaoId", UUID.fromString("00000000-0000-0000-0000-0000000000aa").toString())
                .param("granularidade", "SEMANAL")
                .param("inicio", Instant.parse("2026-10-01T00:00:00Z").toString())
                .param("fim", Instant.parse("2026-10-08T00:00:00Z").toString())
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FIELD_INVALID"))
                .andExpect(jsonPath("$.errors[0].field").value("periodo"));
    }
}
