package br.com.nsfatima.gestao.calendario.contract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ObservacoesContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @BeforeEach
    void setUp() {
        observacaoEventoJpaRepository.deleteAll();
    }

    @Test
    void shouldCreateNotaUsingAuthenticatedAuthor() throws Exception {
        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"tipo\":\"nota\",\"conteudo\":\"Registro pastoral\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.usuarioId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.tipo").value("NOTA"))
                .andExpect(jsonPath("$.conteudo").value("Registro pastoral"))
                .andExpect(jsonPath("$.criadoEmUtc").exists());
    }

    @Test
    void shouldRejectManualCreationForSystemType() throws Exception {
        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"tipo\":\"cancelamento\",\"conteudo\":\"Nao permitido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("OBSERVACAO_TIPO_MANUAL_INVALIDO"));
    }

    @Test
    void shouldListObservacoesWithCreatedTimestamp() throws Exception {
        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"tipo\":\"nota\",\"conteudo\":\"Historico append-only\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$[0].tipo").value("NOTA"))
                .andExpect(jsonPath("$[0].conteudo").value("Historico append-only"))
                .andExpect(jsonPath("$[0].criadoEmUtc").exists());
    }

    @Test
    void shouldListOnlyMyObservacoes() throws Exception {
        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"tipo\":\"nota\",\"conteudo\":\"Minha nota\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                .header("X-Actor-Role", "membro")
                .header("X-Actor-Org-Type", "PASTORAL")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"tipo\":\"nota\",\"conteudo\":\"Outra nota\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes/minhas", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].conteudo").value("Minha nota"));
    }
}
