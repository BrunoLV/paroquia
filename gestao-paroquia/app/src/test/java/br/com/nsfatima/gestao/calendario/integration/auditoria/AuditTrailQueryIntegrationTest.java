package br.com.nsfatima.gestao.calendario.integration.auditoria;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditTrailQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
        observacaoEventoJpaRepository.deleteAll();
        eventoJpaRepository.deleteAll();
    }

    @Test
    @SuppressWarnings("null")
    void shouldQueryPersistedAuditTrailByOrganizationAndPeriod() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID outraOrganizacao = UUID.fromString("00000000-0000-0000-0000-0000000000bb");
        UUID eventoId = persistEvent(organizacaoId, "Evento auditado");
        persistEvent(outraOrganizacao, "Evento fora do escopo");

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Org-Id", organizacaoId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"descricao\":\"Atualizacao auditavel\"" +
                        "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .header("X-Actor-Org-Id", organizacaoId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"tipo\":\"NOTA\"," +
                        "\"conteudo\":\"Observacao auditavel\"" +
                        "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                .param("organizacaoId", organizacaoId.toString())
                .param("inicio", "2026-01-01T00:00:00Z")
                .param("fim", "2027-01-01T00:00:00Z")
                .header("X-Actor-Org-Id", organizacaoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].acao").value("patch"))
                .andExpect(jsonPath("$.items[1].acao").value("create-observacao"));
    }

    private UUID persistEvent(UUID organizacaoId, String titulo) {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo(titulo);
        evento.setDescricao("descricao-base");
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(Instant.parse("2026-10-01T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-01T11:00:00Z"));
        evento.setStatus("RASCUNHO");
        eventoJpaRepository.save(evento);
        return eventoId;
    }
}
