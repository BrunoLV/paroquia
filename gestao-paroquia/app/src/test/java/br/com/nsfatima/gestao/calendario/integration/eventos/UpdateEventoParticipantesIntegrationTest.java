package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class UpdateEventoParticipantesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldPersistParticipantesWhenAuthorizedCoordinatorPatches() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Participantes PATCH");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-07-10T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-07-10T11:00:00Z"));
        entity.setStatus("RASCUNHO");
        eventoJpaRepository.save(entity);

        String payload = """
                {
                  "participantes": [
                    "00000000-0000-0000-0000-0000000000bb",
                    "00000000-0000-0000-0000-0000000000dd"
                  ]
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "vice-coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isOk());

        assertThat(eventoEnvolvidoJpaRepository.findByEventoId(eventoId)).hasSize(2);
    }
}
