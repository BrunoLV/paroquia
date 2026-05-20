package br.com.nsfatima.gestao.calendario.integration.observacoes;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListMyObservacoesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @BeforeEach
    void setUp() {
        observacaoEventoJpaRepository.deleteAll();
    }

    @Test
    void shouldReturnOnlyAuthenticatedUserNotesInMinhasMode() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000aa";

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Nota do usuario padrao\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .header("X-Actor-Role", "membro")
                .header("X-Actor-Org-Type", "PASTORAL")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Nota de outro usuario\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes/minhas", eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].conteudo").value("Nota do usuario padrao"));
    }
}
