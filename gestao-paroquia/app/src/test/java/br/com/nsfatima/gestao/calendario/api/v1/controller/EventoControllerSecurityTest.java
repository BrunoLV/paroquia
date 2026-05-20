package br.com.nsfatima.gestao.calendario.api.v1.controller;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class EventoControllerSecurityTest {

    private static final String ORG_DEFAULT = "00000000-0000-0000-0000-000000000001";
    private static final String PASTORAL_A = "00000000-0000-0000-0000-0000000000aa";
    private static final String PASTORAL_B = "00000000-0000-0000-0000-0000000000bb";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    void shouldAllowParocoToViewAnyEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        createEvento(eventId, UUID.fromString(PASTORAL_A));

        mockMvc.perform(get("/api/v1/eventos/" + eventId)
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowOrgMemberToViewTheirOwnOrgEvent() throws Exception {
        UUID orgId = UUID.fromString(PASTORAL_A);
        UUID eventId = UUID.randomUUID();
        createEvento(eventId, orgId);

        mockMvc.perform(get("/api/v1/eventos/" + eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", orgId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyOrgMemberFromViewingOtherOrgEvent() throws Exception {
        UUID otherOrgId = UUID.fromString(PASTORAL_B);
        UUID myOrgId = UUID.fromString(PASTORAL_A);
        UUID eventId = UUID.randomUUID();
        createEvento(eventId, otherOrgId);

        mockMvc.perform(get("/api/v1/eventos/" + eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", myOrgId.toString()))
                .andExpect(status().isForbidden());
    }

    private void createEvento(UUID id, UUID orgId) {
        EventoEntity entity = new EventoEntity();
        entity.setId(id);
        entity.setTitulo("Evento Privado");
        entity.setOrganizacaoResponsavelId(orgId);
        entity.setInicioUtc(Instant.parse("2026-05-15T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-05-15T11:00:00Z"));
        entity.setStatus("AGENDADO");
        eventoJpaRepository.save(entity);
    }
}
