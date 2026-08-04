package br.com.nsfatima.gestao.calendario.integration.aprovacao;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@SpringBootTest
@AutoConfigureMockMvc
class AprovacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AprovacaoJpaRepository aprovacaoRepository;

    @Autowired
    private EventoJpaRepository eventoRepository;

    @Autowired
    private EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaRepository;

    @BeforeEach
    void setUp() {
        auditoriaRepository.deleteAllInBatch();
        observacaoEventoJpaRepository.deleteAllInBatch();
        aprovacaoRepository.deleteAllInBatch();
        eventoEnvolvidoJpaRepository.deleteAllInBatch();
        eventoRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Should perform full aprovacao lifecycle starting from event creation")
    void deveRealizarCompletoCicloVida() throws Exception {
        String orgId = "00000000-0000-0000-0000-000000000001";

        // 1. Request Event Creation (Requires Aprovacao)
        String createEventPayload = """
                {
                  "titulo": "Evento Pendente",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-12-01T10:00:00Z",
                  "fim": "2026-12-01T12:00:00Z",
                  "status": "CONFIRMADO"
                }
                """.formatted(orgId);

        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEventPayload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();

        String createEventResponse = createResult.getResponse().getContentAsString();
        String aprovacaoId = JsonPath.read(createEventResponse, "$.solicitacaoAprovacaoId");

        // 2. List Aprovacaos (Verify it's there)
        mockMvc.perform(get("/api/v1/aprovacoes")
                        .param("status", "PENDENTE")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(aprovacaoId)).exists());

        // 3. Decide (Approve)
        String decisionPayload = """
                {
                  "status": "APROVADA",
                  "observacao": "Aprovado via teste"
                }
                """;

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decisionPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"));

        // 4. Verify Event was created and confirmed
        UUID createdEventoId = aprovacaoRepository.findById(UUID.fromString(aprovacaoId)).get().getEventoId();

        mockMvc.perform(get("/api/v1/eventos/{id}", createdEventoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }
}
