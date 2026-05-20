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
class UpdateEventoOrganizacaoRulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldDenyOwnerChangeForResponsibleOrganizationCoordinator() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Owner change");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-06-10T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-06-10T11:00:00Z"));
        entity.setStatus("RASCUNHO");
        eventoJpaRepository.save(entity);

        String payload = """
                {
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb"
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    @SuppressWarnings("null")
    void shouldAllowOwnerChangeForConselhoCoordinator() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Owner change allowed");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-06-11T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-06-11T11:00:00Z"));
        entity.setStatus("RASCUNHO");
        eventoJpaRepository.save(entity);

        String payload = """
                {
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb"
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000cc")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizacaoResponsavelId").value("00000000-0000-0000-0000-0000000000bb"));
    }
}
