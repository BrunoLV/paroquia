package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("Should return structured error for missing required fields")
    void shouldReturnErrorForMissingFields() throws Exception {
        String payload = "{}";

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "err-test-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[?(@.field == 'titulo')].code").value(ErrorCodes.VALIDATION_REQUIRED_FIELD.name()))
                .andExpect(jsonPath("$.errors[?(@.field == 'organizacaoResponsavelId')].code").value(ErrorCodes.VALIDATION_REQUIRED_FIELD.name()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return structured error for too long title")
    void shouldReturnErrorForLongTitle() throws Exception {
        String longTitle = "A".repeat(161);
        String payload = """
                {
                  "titulo": "%s",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-000000000001",
                  "inicio": "2026-06-11T17:00:00Z",
                  "fim": "2026-06-11T18:00:00Z"
                }
                """.formatted(longTitle);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "err-test-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.errors[?(@.field == 'titulo')].code").value(ErrorCodes.VALIDATION_FIELD_INVALID.name()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return DOMAIN_RULE_VIOLATION for end date before start date")
    void shouldReturnDomainViolationForInvalidDates() throws Exception {
        String payload = """
                {
                  "titulo": "Valid Title",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-000000000001",
                  "inicio": "2026-06-11T18:00:00Z",
                  "fim": "2026-06-11T17:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "err-test-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCodes.DOMAIN_RULE_VIOLATION.name()))
                .andExpect(jsonPath("$.errors[0].code").value(ErrorCodes.DOMAIN_RULE_VIOLATION.name()));
    }
}
