package br.com.nsfatima.gestao.calendario.integration.observacoes;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoNotaRevisaoJpaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateObservacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Autowired
    private ObservacaoNotaRevisaoJpaRepository observacaoNotaRevisaoJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        observacaoNotaRevisaoJpaRepository.deleteAll();
        observacaoEventoJpaRepository.deleteAll();
    }

    @Test
    void shouldUpdateNoteAndPersistRevision() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000bb";
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Versao 1\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID observacaoId = UUID.fromString(created.path("id").asText());

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}/observacoes/{observacaoId}", eventoId, observacaoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"conteudo\":\"Versao 2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").value("Versao 2"));

        assertThat(observacaoNotaRevisaoJpaRepository.findByObservacaoIdOrderByRevisadoEmUtcAscIdAsc(observacaoId))
                .hasSize(1)
                .first()
                .extracting(revisao -> revisao.getConteudoAnterior(), revisao -> revisao.getConteudoNovo())
                .containsExactly("Versao 1", "Versao 2");
    }
}
