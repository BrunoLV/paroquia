package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
    void deveRetornarRetrabalhoContratoPayload() throws Exception {
        UUID grupoParoquialId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();

        auditoriaOperacaoJpaRepository.save(reworkRecord(
                UUID.randomUUID(), grupoParoquialId, eventoId));

        mockMvc.perform(get("/api/v1/auditoria/eventos/retrabalho")
                        .param("grupoParoquialId", grupoParoquialId.toString())
                        .param("inicio", "2026-10-01T00:00:00Z")
                        .param("fim", "2026-10-10T00:00:00Z")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupoParoquialId").value(grupoParoquialId.toString()))
                .andExpect(jsonPath("$.periodo.inicio").value("2026-10-01T00:00:00Z"))
                .andExpect(jsonPath("$.taxaRetrabalho").value(1.0))
                .andExpect(jsonPath("$.numeradorOcorrenciasElegiveis").value(1))
                .andExpect(jsonPath("$.denominadorEventosAfetados").value(1));
    }

    private AuditoriaOperacaoEntity reworkRecord(UUID id, UUID grupoParoquialId, UUID eventoId) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(id);
        entity.setGrupoParoquialId(grupoParoquialId);
        entity.setEventoId(eventoId);
        entity.setRecursoTipo("EVENTO");
        entity.setRecursoId(eventoId.toString());
        entity.setAcao("atualizar");
        entity.setResultado("sucesso");
        entity.setAtor("tester");
        entity.setCorrelationId("corr-rework");
        entity.setDetalhesAuditaveisJson("{\"agendaAlterada\":true}");
        entity.setOcorridoEmUtc(Instant.parse("2026-10-03T10:15:00Z"));
        return entity;
    }
}
