package br.com.nsfatima.gestao.calendario.integration.eventos;

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
class EventoOrganizacaoParticipantesIntegrityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser
  void shouldRejectWhenResponsibleOrganizationIsRepeatedInParticipants() throws Exception {
    String payload = """
        {
          "titulo": "Planejamento",
          "organizacaoResponsavelId": "00000000-0000-0000-0000-000000000001",
          "inicio": "2026-06-01T10:00:00Z",
          "fim": "2026-06-01T11:00:00Z",
          "participantes": [
            "00000000-0000-0000-0000-000000000001",
            "00000000-0000-0000-0000-000000000001"
          ]
        }
        """;

    mockMvc.perform(post("/api/v1/eventos")
        .header("Idempotency-Key", "evt-org-participantes-001")
        .header("X-Actor-Role", "paroco")
        .header("X-Actor-Org-Type", "CLERO")
        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-000000000001")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("DOMAIN_RULE_VIOLATION"));
  }
}
