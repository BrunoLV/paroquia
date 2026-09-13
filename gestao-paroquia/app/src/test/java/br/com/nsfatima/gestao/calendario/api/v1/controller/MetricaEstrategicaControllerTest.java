package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.CoeficienteImpactoSocialResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.ConcorrenciaInterpastoralResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.OciosidadePatrimonialResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SazonalidadeLiturgicaResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SlaDespachoPastoralResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaConclusaoProjetosResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularImpactoSocialUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularOciosidadePatrimonialUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularSazonalidadeLiturgicaUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularSlaDespachoPastoralUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularTaxaProjetosUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.VerificarConcorrenciaInterpastoralUseCase;
import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class MetricaEstrategicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalcularSazonalidadeLiturgicaUseCase calcularSazonalidadeLiturgicaUseCase;

    @MockitoBean
    private CalcularTaxaProjetosUseCase calcularTaxaProjetosUseCase;

    @MockitoBean
    private CalcularSlaDespachoPastoralUseCase calcularSlaDespachoPastoralUseCase;

    @MockitoBean
    private CalcularImpactoSocialUseCase calcularImpactoSocialUseCase;

    @MockitoBean
    private VerificarConcorrenciaInterpastoralUseCase verificarConcorrenciaInterpastoralUseCase;

    @MockitoBean
    private CalcularOciosidadePatrimonialUseCase calcularOciosidadePatrimonialUseCase;

    @Test
    @DisplayName("Deve retornar 200 e o painel consolidado com as 6 métricas estratégicas")
    void deveRetornarPainelConsolidado() throws Exception {
        SazonalidadeLiturgicaResponse saz = new SazonalidadeLiturgicaResponse("Quaresma", TipoCicloLiturgico.QUARESMA,
                BigDecimal.valueOf(2.5), 25L, 62L, BigDecimal.valueOf(2.5), "PICO_OPERACIONAL");
        TaxaConclusaoProjetosResponse proj = new TaxaConclusaoProjetosResponse(10L, 8L, 2L, 0L,
                BigDecimal.valueOf(80.0), BigDecimal.valueOf(0.0), "SAUDAVEL");
        SlaDespachoPastoralResponse sla = new SlaDespachoPastoralResponse(5L, 5L, 0L, BigDecimal.valueOf(12.0),
                BigDecimal.valueOf(48.0), BigDecimal.valueOf(100.0), "EXCELENTE");
        CoeficienteImpactoSocialResponse soc = new CoeficienteImpactoSocialResponse(
                20L, 12L, 8L, BigDecimal.valueOf(60.0), "MISSIONARIA_EM_SAIDA");
        ConcorrenciaInterpastoralResponse conc = new ConcorrenciaInterpastoralResponse(0, List.of(), "BAIXO");
        OciosidadePatrimonialResponse oci = new OciosidadePatrimonialResponse(5L, BigDecimal.valueOf(200.0),
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(150.0), BigDecimal.valueOf(75.0),
                "OPORTUNIDADE_DE_NOVAS_FRENTES");

        when(calcularSazonalidadeLiturgicaUseCase.execute()).thenReturn(List.of(saz));
        when(calcularTaxaProjetosUseCase.execute()).thenReturn(proj);
        when(calcularSlaDespachoPastoralUseCase.execute()).thenReturn(sla);
        when(calcularImpactoSocialUseCase.execute()).thenReturn(soc);
        when(verificarConcorrenciaInterpastoralUseCase.execute()).thenReturn(conc);
        when(calcularOciosidadePatrimonialUseCase.execute()).thenReturn(oci);

        mockMvc.perform(get("/api/v1/metricas/estrategicas/painel")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sazonalidadeLiturgica[0].cicloNome").value("Quaresma"))
                .andExpect(jsonPath("$.taxaProjetos.avaliacaoExecutiva").value("SAUDAVEL"))
                .andExpect(jsonPath("$.slaDespacho.statusSla").value("EXCELENTE"))
                .andExpect(jsonPath("$.impactoSocial.diagnosticoPastoral").value("MISSIONARIA_EM_SAIDA"))
                .andExpect(jsonPath("$.concorrenciaInterpastoral.nivelRiscoConcorrencia").value("BAIXO"))
                .andExpect(jsonPath("$.ociosidadePatrimonial.recomendacaoCae").value("OPORTUNIDADE_DE_NOVAS_FRENTES"));
    }
}
