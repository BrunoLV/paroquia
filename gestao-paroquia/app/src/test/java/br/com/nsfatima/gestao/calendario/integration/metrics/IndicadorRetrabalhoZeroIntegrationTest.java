package br.com.nsfatima.gestao.calendario.integration.metrics;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class IndicadorRetrabalhoZeroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
    }

    @Test
    void shouldReturnZeroWhenNoEligibleOccurrencesExist() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

        mockMvc.perform(get("/api/v1/auditoria/eventos/retrabalho")
                .param("organizacaoId", organizacaoId.toString())
                .param("inicio", "2026-10-01T00:00:00Z")
                .param("fim", "2026-10-10T00:00:00Z")
                .header("X-Actor-Org-Id", organizacaoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxaRetrabalho").value(0.0))
                .andExpect(jsonPath("$.numeradorOcorrenciasElegiveis").value(0))
                .andExpect(jsonPath("$.denominadorEventosAfetados").value(0));
    }
}
