package br.com.nsfatima.gestao.calendario.integration.metrics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class TaxaEventosExtraIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";
    private static final String OTHER_ORG_ID = "00000000-0000-0000-0000-0000000000bb";

    @BeforeEach
    void setUp() {
        eventoJpaRepository.deleteAll();
    }

    @Test
    void shouldCalculateTaxaEventosExtra() throws Exception {
        UUID organizacaoId = UUID.fromString(ORG_ID);
        
        // 1. Regular event
        eventoJpaRepository.save(createEvento(organizacaoId, "Evento Regular", "AGENDADO", "2026-10-05T10:00:00Z"));
        
        // 2. Extra event
        eventoJpaRepository.save(createEvento(organizacaoId, "Evento Extra 1", "ADICIONADO_EXTRA", "2026-10-06T10:00:00Z"));
        
        // 3. Extra event
        eventoJpaRepository.save(createEvento(organizacaoId, "Evento Extra 2", "ADICIONADO_EXTRA", "2026-10-07T10:00:00Z"));
        
        // 4. Regular event (another org - should not count)
        eventoJpaRepository.save(createEvento(UUID.fromString(OTHER_ORG_ID), "Outra Org", "ADICIONADO_EXTRA", "2026-10-08T10:00:00Z"));

        mockMvc.perform(get("/api/v1/auditoria/eventos/extras")
                .param("organizacaoId", ORG_ID)
                .param("inicio", "2026-10-01T00:00:00Z")
                .param("fim", "2026-10-31T23:59:59Z")
                .header("X-Actor-Org-Id", ORG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxaEventosAdicionadosExtra").value(66.66666666666666));
    }

    private EventoEntity createEvento(UUID orgId, String titulo, String status, String inicio) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(titulo);
        entity.setOrganizacaoResponsavelId(orgId);
        entity.setStatus(status);
        entity.setInicioUtc(Instant.parse(inicio));
        entity.setFimUtc(Instant.parse(inicio).plusSeconds(3600));
        return entity;
    }
}
