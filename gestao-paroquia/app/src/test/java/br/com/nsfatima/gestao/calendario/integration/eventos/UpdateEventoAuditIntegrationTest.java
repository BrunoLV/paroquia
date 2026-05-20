package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateEventoAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @SpyBean
    private EventoAuditPublisher eventoAuditPublisher;

    @Test
    @WithMockUser(username = "tester-success")
    @SuppressWarnings("null")
    void shouldPublishSuccessAuditOnSuccessfulPatch() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Audit success");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-11-01T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-11-01T11:00:00Z"));
        entity.setStatus("RASCUNHO");
        eventoJpaRepository.save(entity);

        String payload = """
                {
                  "descricao": "auditado"
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isOk());

        verify(eventoAuditPublisher).publish(eq("tester-success"), eq("patch"), eq(eventoId.toString()), eq("success"),
                anyMap());
    }

    @Test
    @WithMockUser(username = "tester-failure")
    void shouldPublishFailureAuditOnFailedPatch() throws Exception {
        String payload = """
                {
                  "descricao": "falha auditada"
                }
                """;

        UUID inexistente = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", inexistente)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isNotFound());

        verify(eventoAuditPublisher).publish(eq("tester-failure"), eq("patch"), eq(inexistente.toString()),
                eq("failure"), anyMap());
    }
}
