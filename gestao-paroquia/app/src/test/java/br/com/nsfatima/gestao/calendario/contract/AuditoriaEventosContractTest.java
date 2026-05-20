package br.com.nsfatima.gestao.calendario.contract;

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
class AuditoriaEventosContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
    }

    @Test
    void shouldReturnAuditTrailContractPayload() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();

        auditoriaOperacaoJpaRepository.save(auditRecord(
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
                organizacaoId,
                eventoId,
                "patch",
                "success",
                Instant.parse("2026-10-03T10:15:00Z"),
                "{\"scheduleChanged\":true}"));

        mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                .param("organizacaoId", organizacaoId.toString())
                .param("inicio", "2026-10-01T00:00:00Z")
                .param("fim", "2026-10-10T00:00:00Z")
                .header("X-Actor-Org-Id", organizacaoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizacaoId").value(organizacaoId.toString()))
                .andExpect(jsonPath("$.periodo.inicio").value("2026-10-01T00:00:00Z"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].recursoTipo").value("EVENTO"))
                .andExpect(jsonPath("$.items[0].acao").value("patch"))
                .andExpect(jsonPath("$.items[0].detalhesAuditaveis.scheduleChanged").value(true));
    }

    private AuditoriaOperacaoEntity auditRecord(
            UUID id,
            UUID organizacaoId,
            UUID eventoId,
            String acao,
            String resultado,
            Instant ocorridoEmUtc,
            String detalhesJson) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(id);
        entity.setOrganizacaoId(organizacaoId);
        entity.setEventoId(eventoId);
        entity.setRecursoTipo("EVENTO");
        entity.setRecursoId(eventoId.toString());
        entity.setAcao(acao);
        entity.setResultado(resultado);
        entity.setAtor("tester");
        entity.setCorrelationId("corr-contract");
        entity.setDetalhesAuditaveisJson(detalhesJson);
        entity.setOcorridoEmUtc(ocorridoEmUtc);
        return entity;
    }
}
