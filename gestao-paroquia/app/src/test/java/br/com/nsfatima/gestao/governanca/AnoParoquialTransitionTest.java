package br.com.nsfatima.gestao.governanca;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

    private int anoCorrente;

    @BeforeEach
    void setup() {
        repository.deleteAll();
        anoCorrente = LocalDate.now().getYear();
    }

    @Test
    void devePermitirTransicionarParaEmPlanejamentoMesmoSeAnteriorAnoENaoConcluido() throws Exception {
        // Arrange: ano anterior existe mas não está CONCLUIDO (está EM_PLANEJAMENTO)
        saveAno(anoCorrente - 1, AnoParoquialStatus.EM_PLANEJAMENTO);

        // Act & Assert: tenta definir o ano corrente como EM_PLANEJAMENTO (permitido)
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + anoCorrente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"EM_PLANEJAMENTO\"}")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk());
    }

    @Test
    void deveFalhaParaTransicionarParaFechadoSeAnteriorAnoENaoConcluido() throws Exception {
        // Arrange: ano anterior existe mas não está CONCLUIDO (está EM_PLANEJAMENTO)
        saveAno(anoCorrente - 1, AnoParoquialStatus.EM_PLANEJAMENTO);

        // Act & Assert: tenta definir o ano corrente como FECHADO (falha)
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + anoCorrente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"FECHADO\"}")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFalhaParaTransicionarParaFechadoSeQualquerPriorAnoENaoConcluido() throws Exception {
        // Arrange: ano N-2 está EM_PLANEJAMENTO, N-1 está CONCLUIDO
        saveAno(anoCorrente - 2, AnoParoquialStatus.EM_PLANEJAMENTO);
        saveAno(anoCorrente - 1, AnoParoquialStatus.CONCLUIDO);

        // Act & Assert: tenta definir o ano corrente como FECHADO (falha porque N-2 não está CONCLUIDO)
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + anoCorrente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"FECHADO\"}")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devePermitirTransicionarParaEmPlanejamentoSeAnteriorAnoEConcluido() throws Exception {
        // Arrange: ano anterior está CONCLUIDO
        saveAno(anoCorrente - 1, AnoParoquialStatus.CONCLUIDO);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + anoCorrente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"EM_PLANEJAMENTO\"}")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk());
    }

    @Test
    void deveFalhaParaTransicionarForaDeEmPlanejamentoSeAnoENaoAtualCalendarioAno() throws Exception {
        // Arrange: um ano futuro está EM_PLANEJAMENTO
        saveAno(anoCorrente - 1, AnoParoquialStatus.CONCLUIDO);
        saveAno(anoCorrente, AnoParoquialStatus.CONCLUIDO);
        saveAno(anoCorrente + 1, AnoParoquialStatus.EM_PLANEJAMENTO);

        // Act & Assert: tenta definir o ano futuro como FECHADO; deve falhar porque não é o ano corrente
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + (anoCorrente + 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"FECHADO\"}")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void devePermitirTransicionarForaDeEmPlanejamentoSeAnoEAtualCalendarioAno() throws Exception {
        // Arrange: ano corrente está EM_PLANEJAMENTO, anterior está CONCLUIDO
        saveAno(anoCorrente - 1, AnoParoquialStatus.CONCLUIDO);
        saveAno(anoCorrente, AnoParoquialStatus.EM_PLANEJAMENTO);

        // Act & Assert: transiciona o ano corrente para FECHADO
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + anoCorrente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"FECHADO\"}")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk());
    }

    @Test
    void deveFalhaParaAtualizarConcluidoAno() throws Exception {
        // Arrange: ano corrente está CONCLUIDO
        saveAno(anoCorrente - 1, AnoParoquialStatus.CONCLUIDO);
        saveAno(anoCorrente, AnoParoquialStatus.CONCLUIDO);

        // Act & Assert: tenta voltar para EM_PLANEJAMENTO; deve falhar
        mockMvc.perform(patch("/api/v1/anos-paroquiais/" + anoCorrente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"EM_PLANEJAMENTO\"}")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isBadRequest());
    }

    private void saveAno(int ano, AnoParoquialStatus status) {
        AnoParoquialEntity entity = new AnoParoquialEntity();
        entity.setAno(ano);
        entity.setStatus(status);
        repository.saveAndFlush(entity);
    }
}
