package br.com.nsfatima.gestao.calendario.integration.auditoria;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class AuditTrailAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
    }

    @Test
    void deveNegarAuditarTrilhaForaGrupoParoquialEscopo() throws Exception {
        UUID grupoParoquialId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();

        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setGrupoParoquialId(grupoParoquialId);
        entity.setEventoId(eventoId);
        entity.setRecursoTipo("EVENTO");
        entity.setRecursoId(eventoId.toString());
        entity.setAcao("patch");
        entity.setResultado("success");
        entity.setAtor("tester");
        entity.setCorrelationId("corr-auth");
        entity.setDetalhesAuditaveisJson("{}");
        entity.setOcorridoEmUtc(Instant.parse("2026-10-03T10:15:00Z"));
        auditoriaOperacaoJpaRepository.save(entity);

        mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                        .param("grupoParoquialId", grupoParoquialId.toString())
                        .param("inicio", "2026-10-01T00:00:00Z")
                        .param("fim", "2026-10-10T00:00:00Z")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb"))
                .andExpect(status().isForbidden());
    }
}
