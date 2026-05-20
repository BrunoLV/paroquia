package br.com.nsfatima.gestao.calendario.integration.observacoes;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
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

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ObservacaoMutationAuthorizationIntegrationTest {

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
    void shouldRejectPatchWhenUserIsNotAuthor() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000bb";
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Nota do autor\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID observacaoId = UUID.fromString(created.path("id").asText());

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}/observacoes/{observacaoId}", eventoId, observacaoId)
                .header("X-Actor-Role", "membro")
                .header("X-Actor-Org-Type", "PASTORAL")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"conteudo\":\"Tentativa invalida\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("OBSERVACAO_AUTOR_INVALIDO"));
    }

    @Test
    void shouldRejectPatchForSystemType() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000bb";
        ObservacaoEventoEntity systemObservacao = new ObservacaoEventoEntity();
        systemObservacao.setId(UUID.randomUUID());
        systemObservacao.setEventoId(UUID.fromString(eventoId));
        systemObservacao.setUsuarioId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        systemObservacao.setTipo("CANCELAMENTO");
        systemObservacao.setConteudo("Gerada automaticamente");
        systemObservacao.setCriadoEmUtc(Instant.now());
        systemObservacao.setRemovida(false);
        observacaoEventoJpaRepository.save(systemObservacao);

        mockMvc.perform(
                patch("/api/v1/eventos/{eventoId}/observacoes/{observacaoId}", eventoId, systemObservacao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"conteudo\":\"Tentativa invalida\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("OBSERVACAO_TIPO_IMUTAVEL"));
    }
}
