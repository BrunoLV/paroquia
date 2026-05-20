package br.com.nsfatima.gestao.calendario.integration.auditoria;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditTrailOrderingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
    }

    @Test
    void shouldOrderAuditTrailDeterministicallyWhenTimestampsMatch() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        Instant sameInstant = Instant.parse("2026-10-03T10:15:00Z");
        UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000111");
        UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000222");
        UUID eventoId = UUID.randomUUID();

        auditoriaOperacaoJpaRepository.save(auditRecord(secondId, organizacaoId, eventoId, sameInstant, "patch"));
        auditoriaOperacaoJpaRepository.save(auditRecord(firstId, organizacaoId, eventoId, sameInstant, "cancel"));

        mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                .param("organizacaoId", organizacaoId.toString())
                .param("inicio", "2026-10-01T00:00:00Z")
                .param("fim", "2026-10-10T00:00:00Z")
                .header("X-Actor-Org-Id", organizacaoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(firstId.toString()))
                .andExpect(jsonPath("$.items[1].id").value(secondId.toString()));
    }

    private AuditoriaOperacaoEntity auditRecord(
            UUID id,
            UUID organizacaoId,
            UUID eventoId,
            Instant occurredAt,
            String acao) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(id);
        entity.setOrganizacaoId(organizacaoId);
        entity.setEventoId(eventoId);
        entity.setRecursoTipo("EVENTO");
        entity.setRecursoId(eventoId.toString());
        entity.setAcao(acao);
        entity.setResultado("success");
        entity.setAtor("tester");
        entity.setCorrelationId("corr-order");
        entity.setDetalhesAuditaveisJson("{}");
        entity.setOcorridoEmUtc(occurredAt);
        return entity;
    }
}
