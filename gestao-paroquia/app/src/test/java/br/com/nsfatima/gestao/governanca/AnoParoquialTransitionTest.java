package br.com.nsfatima.gestao.governanca;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnoParoquialTransitionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnoParoquialJpaRepository repository;

    private int currentYear;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        currentYear = LocalDate.now().getYear();
    }

    @Test
    void shouldFailToTransitionToEmPlanejamentoIfPreviousYearIsNotConcluido() throws Exception {
        // Arrange: previous year exists but is not CONCLUIDO (it is EM_PLANEJAMENTO)
        saveAno(currentYear - 1, AnoParoquialStatus.EM_PLANEJAMENTO);

        // Act & Assert: try to set current year to EM_PLANEJAMENTO
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + currentYear)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"EM_PLANEJAMENTO\"}")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowTransitionToEmPlanejamentoIfPreviousYearIsConcluido() throws Exception {
        // Arrange: previous year is CONCLUIDO
        saveAno(currentYear - 1, AnoParoquialStatus.CONCLUIDO);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + currentYear)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"EM_PLANEJAMENTO\"}")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailToTransitionOutOfEmPlanejamentoIfYearIsNotCurrentCalendarYear() throws Exception {
        // Arrange: a future year is in EM_PLANEJAMENTO
        saveAno(currentYear - 1, AnoParoquialStatus.CONCLUIDO);
        saveAno(currentYear, AnoParoquialStatus.CONCLUIDO);
        saveAno(currentYear + 1, AnoParoquialStatus.EM_PLANEJAMENTO);

        // Act & Assert: try to set future year to FECHADO, should fail because currentYear != currentYear + 1
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + (currentYear + 1))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"FECHADO\"}")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowTransitionOutOfEmPlanejamentoIfYearIsCurrentCalendarYear() throws Exception {
        // Arrange: current year is in EM_PLANEJAMENTO, previous is CONCLUIDO
        saveAno(currentYear - 1, AnoParoquialStatus.CONCLUIDO);
        saveAno(currentYear, AnoParoquialStatus.EM_PLANEJAMENTO);

        // Act & Assert: transition current year to FECHADO
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + currentYear)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"FECHADO\"}")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailToUpdateConcluidoYear() throws Exception {
        // Arrange: current year is CONCLUIDO
        saveAno(currentYear - 1, AnoParoquialStatus.CONCLUIDO);
        saveAno(currentYear, AnoParoquialStatus.CONCLUIDO);

        // Act & Assert: try to transition back to EM_PLANEJAMENTO, should fail
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + currentYear)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"EM_PLANEJAMENTO\"}")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isBadRequest());
    }

    private void saveAno(int ano, AnoParoquialStatus status) {
        AnoParoquialEntity entity = new AnoParoquialEntity();
        entity.setAno(ano);
        entity.setStatus(status.name());
        repository.saveAndFlush(entity);
    }
}
