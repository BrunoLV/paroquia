package br.com.nsfatima.gestao.calendario.integration.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EnumNormalizationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldNormalizeCanonicalEnumOnCreate() throws Exception {
    String payload = """
        {
          \"titulo\": \"Vigilia\",
                                                                \"organizacaoResponsavelId\": \"00000000-0000-0000-0000-0000000000bb\",
          \"inicio\": \"2026-03-15T10:00:00Z\",
          \"fim\": \"2026-03-15T11:00:00Z\",
          \"status\": \"  confirmado  \"
        }
        """;

    mockMvc.perform(post("/api/v1/eventos")
        .header("Idempotency-Key", "evt-enum-normalization-001")
        .header("X-Actor-Role", "paroco")
        .header("X-Actor-Org-Type", "CLERO")
        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(payload))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("CONFIRMADO"));
  }

  @Test
  void shouldRejectLocalizedOrUnsupportedEnumValues() throws Exception {
    String payload = """
        {
          \"status\": \"confirmado_pt\"
        }
        """;

    mockMvc.perform(patch("/api/v1/eventos/{eventoId}", "00000000-0000-0000-0000-000000000001")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ENUM_VALUE_INVALID"))
        .andExpect(jsonPath("$.errors[0].field").value("status"));
  }
}
