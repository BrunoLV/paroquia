package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateEventoApprovalIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private EventoJpaRepository eventoJpaRepository;

        @Test
        @SuppressWarnings("null")
        void shouldRequireApprovalForDateChange() throws Exception {
                UUID eventoId = UUID.randomUUID();
                EventoEntity entity = new EventoEntity();
                entity.setId(eventoId);
                entity.setTitulo("Approval required");
                entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
                entity.setInicioUtc(Instant.parse("2026-08-01T10:00:00Z"));
                entity.setFimUtc(Instant.parse("2026-08-01T11:00:00Z"));
                entity.setStatus("RASCUNHO");
                eventoJpaRepository.save(entity);

                String payload = """
                                {
                                  "inicio": "2026-08-01T12:00:00Z",
                                  "fim": "2026-08-01T13:00:00Z"
                                }
                                """;

                mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "PASTORAL")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isAccepted())
                                .andExpect(jsonPath("$.status").value("PENDENTE"));
        }
}
