package br.com.nsfatima.gestao.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventosContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void shouldListEventosWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldCreateEventoWithCanonicalEnumNormalization() throws Exception {
        String payload = """
                      {
                        \"titulo\": \"Missa\",
                                                \"organizacaoResponsavelId\": \"00000000-0000-0000-0000-0000000000aa\",
                        \"inicio\": \"2026-03-15T10:00:00Z\",
                \"fim\": \"2026-03-15T11:00:00Z\",
                \"status\": \"  adicionado_extra  \",
                \"adicionadoExtraJustificativa\": \"Cobertura extraordinaria\"
                      }
                      """;

        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", "evt-contract-create-001")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ADICIONADO_EXTRA"));
    }
}
