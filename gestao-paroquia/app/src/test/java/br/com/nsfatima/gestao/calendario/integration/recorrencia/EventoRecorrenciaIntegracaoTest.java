package br.com.nsfatima.gestao.calendario.integration.recorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.scheduling.YearlyRecurrenceGeneratorJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class EventoRecorrenciaIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoRepository;

    @Autowired
    private EventoRecorrenciaJpaRepository recorrenciaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoRepository;

    @Autowired
    private YearlyRecurrenceGeneratorJob generationJob;

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";

    @BeforeEach
    void setUp() {
        aprovacaoRepository.deleteAll();
        // Eventos reference recorrencia, so we must nullify or delete in order.
        // Due to circular dependency (evento -> recorrencia -> base_evento), 
        // we use a native query to clear tables safely in H2.
        eventoRepository.deleteAll();
        recorrenciaRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create recurrence, generate instances, and handle ONLY_THIS edit")
    void shouldHandleFullRecurrenceLifecycle_OnlyThis() throws Exception {
        // 1. Create Base Event
        UUID baseId = UUID.randomUUID();
        EventoEntity baseEvent = createEvento(baseId, "Base Recorrente", "CONFIRMADO", "2026-05-10T10:00:00Z");
        eventoRepository.save(baseEvent);

        // 2. Configure Weekly Recurrence (Sundays)
        String rulePayload = """
                {
                    "frequencia": "SEMANAL",
                    "intervalo": 1,
                    "diasDaSemana": ["SUNDAY"]
                }
                """;
        
        mockMvc.perform(put("/api/v1/eventos/{id}/recorrencia", baseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rulePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.frequencia").value("SEMANAL"));

        // 3. Verify Instances were generated (2026-05-10 is Sunday, so it's the first. May has 4 Sundays after that: 17, 24, 31)
        // total count = 1 (base) + some generated. Total generated for 2026 from May 10th should be ~34.
        assertThat(eventoRepository.count()).isGreaterThan(30);

        // 4. Find one instance (e.g., May 17th)
        EventoEntity instance = eventoRepository.findAll().stream()
                .filter(e -> e.getInicioUtc().toString().contains("2026-05-17"))
                .findFirst().orElseThrow();
        UUID instanceId = instance.getId();
        assertThat(instance.getRecorrenciaId()).isNotNull();

        // 5. Edit ONLY_THIS
        String patchPayload = """
                {
                    "titulo": "Missa Especial",
                    "editScope": "ONLY_THIS"
                }
                """;
        
        mockMvc.perform(patch("/api/v1/eventos/{id}", instanceId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Missa Especial"));

        // 6. Verify detached
        EventoEntity updatedInstance = eventoRepository.findById(instanceId).orElseThrow();
        assertThat(updatedInstance.getRecorrenciaId()).isNull();
        assertThat(updatedInstance.getTitulo()).isEqualTo("Missa Especial");

        // 7. Verify next instance still has original title and same recorrenciaId
        EventoEntity nextInstance = eventoRepository.findAll().stream()
                .filter(e -> e.getInicioUtc().toString().contains("2026-05-24"))
                .findFirst().orElseThrow();
        assertThat(nextInstance.getTitulo()).isEqualTo("Base Recorrente");
        assertThat(nextInstance.getRecorrenciaId()).isEqualTo(instance.getRecorrenciaId());
    }

    @Test
    @DisplayName("Should handle THIS_AND_FOLLOWING edit by splitting the series")
    void shouldHandleFullRecurrenceLifecycle_ThisAndFollowing() throws Exception {
        // 1. Create Base Event
        UUID baseId = UUID.randomUUID();
        EventoEntity baseEvent = createEvento(baseId, "Serie Original", "CONFIRMADO", "2026-06-01T10:00:00Z");
        eventoRepository.save(baseEvent);

        // 2. Configure Weekly Recurrence (Mondays)
        String rulePayload = """
                {
                    "frequencia": "SEMANAL",
                    "intervalo": 1,
                    "diasDaSemana": ["MONDAY"]
                }
                """;
        
        mockMvc.perform(put("/api/v1/eventos/{id}/recorrencia", baseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rulePayload))
                .andExpect(status().isOk());

        UUID oldRuleId = recorrenciaRepository.findAll().get(0).getId();

        // 3. Find instance on June 15th
        EventoEntity instance = eventoRepository.findAll().stream()
                .filter(e -> e.getInicioUtc().toString().contains("2026-06-15"))
                .findFirst().orElseThrow();
        UUID instanceId = instance.getId();

        // 4. Edit THIS_AND_FOLLOWING
        String patchPayload = """
                {
                    "titulo": "Nova Serie",
                    "editScope": "THIS_AND_FOLLOWING"
                }
                """;
        
        mockMvc.perform(patch("/api/v1/eventos/{id}", instanceId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchPayload))
                .andExpect(status().isOk());

        // 5. Verify Split
        // Old rule should have dataFimUtc before June 15th
        var oldRule = recorrenciaRepository.findById(oldRuleId).orElseThrow();
        assertThat(oldRule.getDataFimUtc()).isNotNull();
        assertThat(oldRule.getDataFimUtc()).isBefore(Instant.parse("2026-06-15T00:00:00Z"));

        // New rule should exist for "Nova Serie"
        var rules = recorrenciaRepository.findAll();
        assertThat(rules).hasSize(2);
        var newRule = rules.stream().filter(r -> !r.getId().equals(oldRuleId)).findFirst().orElseThrow();
        assertThat(newRule.getEventoBaseId()).isEqualTo(instanceId);

        // Current instance should point to new rule
        EventoEntity updatedInstance = eventoRepository.findById(instanceId).orElseThrow();
        assertThat(updatedInstance.getRecorrenciaId()).isEqualTo(newRule.getId());
        assertThat(updatedInstance.getTitulo()).isEqualTo("Nova Serie");
    }

    private EventoEntity createEvento(UUID id, String titulo, String status, String inicio) {
        EventoEntity entity = new EventoEntity();
        entity.setId(id);
        entity.setTitulo(titulo);
        entity.setOrganizacaoResponsavelId(UUID.fromString(ORG_ID));
        entity.setStatus(status);
        entity.setInicioUtc(Instant.parse(inicio));
        entity.setFimUtc(Instant.parse(inicio).plusSeconds(3600));
        return entity;
    }
}
