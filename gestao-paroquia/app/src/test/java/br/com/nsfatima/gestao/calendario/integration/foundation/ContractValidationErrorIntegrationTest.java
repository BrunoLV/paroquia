package br.com.nsfatima.gestao.calendario.integration.foundation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContractValidationErrorIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void shouldReturnStructuredValidationPayloadForMissingRequiredField() throws Exception {
                String payload = """
                                {
                                  \"organizacaoResponsavelId\": \"00000000-0000-0000-0000-0000000000cc\",
                                  \"inicio\": \"2026-03-15T10:00:00Z\",
                                  \"fim\": \"2026-03-15T11:00:00Z\"
                                }
                                """;

                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-validation-required-001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(header().exists("X-Correlation-Id"))
                                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.correlationId").isString())
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_REQUIRED_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("titulo"));
        }

        @Test
        void shouldRejectUnknownFieldsWithDeterministicValidationCode() throws Exception {
                String payload = """
                                {
                                  \"titulo\": \"Missa\",
                                                                                        \"organizacaoResponsavelId\": \"00000000-0000-0000-0000-0000000000cc\",
                                  \"inicio\": \"2026-03-15T10:00:00Z\",
                                  \"fim\": \"2026-03-15T11:00:00Z\",
                                  \"campoExtra\": true
                                }
                                """;

                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-validation-unknown-001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_UNKNOWN_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("campoExtra"))
                                .andExpect(jsonPath("$.errors[0].rejectedValue").value("campoExtra"));
        }
}
