package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CancelEventoAuditTrailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @SpyBean
    private EventoAuditPublisher eventoAuditPublisher;

    @Test
    @SuppressWarnings("null")
    void shouldPublishAuditForPendingAndExecutedPaths() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Audit trail cancelamento");
        evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-12-10T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-12-10T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        String pending = mockMvc.perform(delete("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"motivo\": \"Gerar trilha de pending\"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        UUID aprovacaoId = UUID
                .fromString(pending.replaceAll(".*\\\"solicitacaoAprovacaoId\\\":\\\"([^\\\"]+)\\\".*", "$1"));

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"status\": \"APROVADA\",
                          \"observacao\": \"Aprovar e executar\"
                        }
                        """))
                .andExpect(status().isOk());

        verify(eventoAuditPublisher, atLeastOnce()).publishCancellationPending(eq("joao.silva"),
                eq(eventoId.toString()), eq(aprovacaoId.toString()), eq("Gerar trilha de pending"));
        verify(eventoAuditPublisher, atLeastOnce()).publishApprovalDecision(eq("ana.conselho"),
                any(), eq("executed"), anyMap());
        verify(eventoAuditPublisher, atLeastOnce()).publish(eq("system"), eq("approval-decision-request"),
                eq(aprovacaoId.toString()), eq("received"), anyMap());
    }
}
