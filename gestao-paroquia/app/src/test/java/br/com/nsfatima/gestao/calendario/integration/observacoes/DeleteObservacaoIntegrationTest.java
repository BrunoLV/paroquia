package br.com.nsfatima.gestao.calendario.integration.observacoes;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void deveSoftExcluirEOcultarDeFuncionalListar() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000bb";
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Para remover\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID observacaoId = UUID.fromString(created.path("id").asString());

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}/observacoes/{observacaoId}", eventoId, observacaoId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
