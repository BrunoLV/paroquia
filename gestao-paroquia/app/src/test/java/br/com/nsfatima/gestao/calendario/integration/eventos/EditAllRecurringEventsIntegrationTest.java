package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class EditAllRecurringEventsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private EventoRecorrenciaJpaRepository recurrenceRepository;

    @Test
    @DisplayName("Should update all instances in a series when scope is EDIT_ALL")
    void shouldUpdateAllInstances() throws Exception {
        String orgId = "00000000-0000-0000-0000-000000000001";
        UUID recorrenciaId = UUID.randomUUID();

        // 1. Setup multiple event instances first (to be siblings or base)
        EventoEntity instance1 = createInstance("Occurrence 1", orgId, "2026-12-01T10:00:00Z");
        EventoEntity instance2 = createInstance("Occurrence 2", orgId, "2026-12-08T10:00:00Z");

        // 2. Setup recurrence rule linking to instance 1 as base
        EventoRecorrenciaEntity rule = new EventoRecorrenciaEntity();
        rule.setId(recorrenciaId);
        rule.setEventoBaseId(instance1.getId());
        rule.setRegra(new RegraRecorrencia("SEMANAL", 1, null, null, null, null, null, null, null));
        recurrenceRepository.save(rule);

        // 3. Link instances to the rule
        instance1.setRecorrenciaId(recorrenciaId);
        instance2.setRecorrenciaId(recorrenciaId);
        eventoJpaRepository.save(instance1);
        eventoJpaRepository.save(instance2);

        // 4. Perform EDIT_ALL update on instance 1
        String payload = """
                {
                  "titulo": "Updated Series Title",
                  "editScope": "EDIT_ALL"
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{id}", instance1.getId())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // 5. Verify both instances and base are updated
        assertEquals("Updated Series Title", eventoJpaRepository.findById(instance1.getId()).get().getTitulo());
        assertEquals("Updated Series Title", eventoJpaRepository.findById(instance2.getId()).get().getTitulo());
    }

    private EventoEntity createInstance(String title, String orgId, String start) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(title);
        entity.setOrganizacaoResponsavelId(UUID.fromString(orgId));
        entity.setRecorrenciaId(null);
        entity.setInicioUtc(Instant.parse(start));
        entity.setFimUtc(Instant.parse(start).plusSeconds(3600));
        entity.setStatus("CONFIRMADO");
        return eventoJpaRepository.save(entity);
    }
}
