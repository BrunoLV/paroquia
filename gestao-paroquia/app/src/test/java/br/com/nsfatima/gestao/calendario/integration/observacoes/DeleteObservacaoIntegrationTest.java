package br.com.nsfatima.gestao.calendario.integration.observacoes;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeleteObservacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        observacaoEventoJpaRepository.deleteAll();
    }

    @Test
    void shouldSoftDeleteAndHideFromFunctionalList() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000bb";
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Para remover\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID observacaoId = UUID.fromString(created.path("id").asText());

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}/observacoes/{observacaoId}", eventoId, observacaoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
