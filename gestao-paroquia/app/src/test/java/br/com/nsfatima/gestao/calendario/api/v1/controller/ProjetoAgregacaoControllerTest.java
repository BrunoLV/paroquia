package br.com.nsfatima.gestao.calendario.api.v1.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.projeto.api.v1.dto.MapaColaboracaoDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.SaudeTemporalDTO;
import br.com.nsfatima.gestao.projeto.api.v1.dto.StatusExecucaoDTO;
import br.com.nsfatima.gestao.projeto.application.usecase.ProjetoAgregacaoService;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoAgregacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjetoAgregacaoService aggregationService;

    @Test
    @DisplayName("Deve retornar 200 e o resumo do projeto")
    void deveRetornarResumoDoProjeto() throws Exception {
        UUID projetoId = UUID.randomUUID();
        ProjetoResumoDTO resumo = new ProjetoResumoDTO(
                new StatusExecucaoDTO(10, 6, 4),
                new MapaColaboracaoDTO(List.of("Org 1")),
                new SaudeTemporalDTO(50.0, false)
        );

        when(aggregationService.obterResumo(projetoId)).thenReturn(resumo);

        mockMvc.perform(get("/api/v1/projetos/{projetoId}/resumo", projetoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusExecucao.totalEventos").value(10))
                .andExpect(jsonPath("$.mapaColaboracao.envolvidos[0]").value("Org 1"))
                .andExpect(jsonPath("$.saudeTemporal.emRisco").value(false));
    }

    @Test
    @DisplayName("Deve retornar 404 quando projeto não existe")
    void deveRetornar404QuandoProjetoNaoExiste() throws Exception {
        UUID projetoId = UUID.randomUUID();
        when(aggregationService.obterResumo(projetoId)).thenThrow(new ProjetoNotFoundException(projetoId));

        mockMvc.perform(get("/api/v1/projetos/{projetoId}/resumo", projetoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
