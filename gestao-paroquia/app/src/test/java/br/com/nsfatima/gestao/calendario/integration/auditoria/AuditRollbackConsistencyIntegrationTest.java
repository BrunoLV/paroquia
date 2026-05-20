package br.com.nsfatima.gestao.calendario.integration.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditRollbackConsistencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;


    @AfterEach
    void tearDown() {
        reset(auditoriaOperacaoJpaRepository);
    }

    @Test
    @SuppressWarnings("null")
    void shouldKeepOriginalStateWhenAuditRollbackIsTriggered() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Rollback auditavel");
        evento.setDescricao("estado-estavel");
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(Instant.parse("2026-10-01T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-01T11:00:00Z"));
        evento.setStatus("RASCUNHO");
        eventoJpaRepository.save(evento);

        doThrow(new RuntimeException("db down"))
                .when(auditoriaOperacaoJpaRepository)
                .save(org.mockito.ArgumentMatchers.<AuditoriaOperacaoEntity>any());

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Org-Id", organizacaoId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"titulo\":\"estado-mutado\"" +
                        "}"))
                .andExpect(status().isConflict());

        EventoEntity persisted = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(persisted.getTitulo()).isEqualTo("Rollback auditavel");
        assertThat(persisted.getDescricao()).isEqualTo("estado-estavel");
    }

    @Test
    @SuppressWarnings("null")
    void shouldKeepEventConfirmedWhenCancellationAuditPersistenceFails() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Cancelamento fail-closed");
        evento.setDescricao("nao cancelar");
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(Instant.parse("2026-10-02T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-02T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        doThrow(new RuntimeException("db down"))
                .when(auditoriaOperacaoJpaRepository)
                .save(any(AuditoriaOperacaoEntity.class));

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", organizacaoId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                        "\"motivo\":\"cancelamento bloqueado\"" +
                        "}"))
                .andExpect(status().isConflict());

        EventoEntity persisted = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo("CONFIRMADO");
        assertThat(persisted.getCanceladoMotivo()).isNull();
    }

    @Test
    @SuppressWarnings("null")
    void shouldKeepObservationVisibleWhenDeleteAuditPersistenceFails() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000bb";
        String organizacaoId = "00000000-0000-0000-0000-0000000000aa";
        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.fromString(eventoId));
        evento.setTitulo("Observacao rollback");
        evento.setDescricao("evento materializado");
        evento.setOrganizacaoResponsavelId(UUID.fromString(organizacaoId));
        evento.setInicioUtc(Instant.parse("2026-10-03T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-03T11:00:00Z"));
        evento.setStatus("RASCUNHO");
        eventoJpaRepository.save(evento);

        var createResult = mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .header("X-Actor-Org-Id", organizacaoId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Para rollback\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID observacaoId = UUID.fromString(created.path("id").asText());

        doThrow(new RuntimeException("db down"))
                .when(auditoriaOperacaoJpaRepository)
                .save(org.mockito.ArgumentMatchers.<AuditoriaOperacaoEntity>any());

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}/observacoes/{observacaoId}", eventoId, observacaoId)
                .header("X-Actor-Org-Id", organizacaoId))
                .andExpect(status().isConflict());

        assertThat(observacaoEventoJpaRepository.findById(Objects.requireNonNull(observacaoId)))
                .isPresent()
                .get()
                .extracting(observacao -> observacao.isRemovida())
                .isEqualTo(false);

        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                .header("X-Actor-Org-Id", organizacaoId))
                .andExpect(status().isOk())
                .andExpect(status().isOk());
    }
}
