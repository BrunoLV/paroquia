package br.com.nsfatima.gestao.calendario.integration.eventos;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.jdbc.Sql(scripts = "classpath:sql/security-fixtures.sql")
class LegacyEventoReadCompatibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @WithMockUser
    @SuppressWarnings("null")
    void shouldMapUnknownLegacyStatusToUnknownLegacyOnRead() throws Exception {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo("Evento Legado");
        entity.setDescricao("status legado");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-05-10T08:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-05-10T09:00:00Z"));
        entity.setStatus("STATUS_OBSOLETO");
        eventoJpaRepository.save(entity);

        mockMvc.perform(get("/api/v1/eventos?size=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status").value(hasItem("UNKNOWN_LEGACY")));


    }
}
