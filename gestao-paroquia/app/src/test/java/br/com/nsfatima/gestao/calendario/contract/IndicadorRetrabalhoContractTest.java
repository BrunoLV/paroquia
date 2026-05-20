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
class IndicadorRetrabalhoContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
    }

    @Test
    void shouldReturnReworkContractPayload() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();

        auditoriaOperacaoJpaRepository.save(reworkRecord(
                UUID.randomUUID(), organizacaoId, eventoId, "patch", "{\"scheduleChanged\":true}"));

        mockMvc.perform(get("/api/v1/auditoria/eventos/retrabalho")
                .param("organizacaoId", organizacaoId.toString())
                .param("inicio", "2026-10-01T00:00:00Z")
                .param("fim", "2026-10-10T00:00:00Z")
                .header("X-Actor-Org-Id", organizacaoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizacaoId").value(organizacaoId.toString()))
                .andExpect(jsonPath("$.periodo.inicio").value("2026-10-01T00:00:00Z"))
                .andExpect(jsonPath("$.taxaRetrabalho").value(1.0))
                .andExpect(jsonPath("$.numeradorOcorrenciasElegiveis").value(1))
                .andExpect(jsonPath("$.denominadorEventosAfetados").value(1));
    }

    private AuditoriaOperacaoEntity reworkRecord(UUID id, UUID organizacaoId, UUID eventoId, String acao,
            String details) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(id);
        entity.setOrganizacaoId(organizacaoId);
        entity.setEventoId(eventoId);
        entity.setRecursoTipo("EVENTO");
        entity.setRecursoId(eventoId.toString());
        entity.setAcao(acao);
        entity.setResultado("success");
        entity.setAtor("tester");
        entity.setCorrelationId("corr-rework");
        entity.setDetalhesAuditaveisJson(details);
        entity.setOcorridoEmUtc(Instant.parse("2026-10-03T10:15:00Z"));
        return entity;
    }
}
