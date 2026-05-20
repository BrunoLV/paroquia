package br.com.nsfatima.gestao.calendario.integration.observacoes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RejectSystemObservationManualCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectManualCreationForReservedSystemType() throws Exception {
        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"CANCELAMENTO\",\"conteudo\":\"Nao permitido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("OBSERVACAO_TIPO_MANUAL_INVALIDO"));
    }
}
