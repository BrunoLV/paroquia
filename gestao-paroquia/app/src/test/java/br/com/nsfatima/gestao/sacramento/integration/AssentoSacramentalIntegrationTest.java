package br.com.nsfatima.gestao.sacramento.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssentoSacramentalIntegrationTest {

    private static final String BASE = "/api/v1/sacramentos";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbc;

    private ResultActions comoParoco(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.with(user("paroco").roles("PAROCO")));
    }

    private static String teorBatismo(String termo) {
        return """
                {"tipo":"BATISMO","teor":{
                  "livro":"7","folha":"3","termo":"%s",
                  "paroquiaCustodiante":"Paróquia N. Sra. de Fátima","dataCelebracao":"2026-03-01T10:00:00",
                  "localCelebracao":"Matriz",
                  "dadosEspecificos":{"certidaoNascimentoCartorio":"1º Ofício","certidaoNascimentoTermo":"9",
                                      "estadoFiliacao":"MATRIMONIAL"},
                  "participantes":[
                    {"papel":"BATIZANDO","nomeHistorico":"Maria da Silva","localNascimento":"São Paulo/SP/BR"},
                    {"papel":"MAE","nomeHistorico":"Ana da Silva"},
                    {"papel":"MINISTRO_CELEBRANTE","nomeHistorico":"Pe. João","condicaoCanonica":"PRESBITERO"}]}}
                """.formatted(termo);
    }

    private JsonNode lavrar(String termo) throws Exception {
        MvcResult r = comoParoco(post(BASE).contentType(MediaType.APPLICATION_JSON).content(teorBatismo(termo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "secretaria", roles = "SECRETARIA_PAROQUIAL")
    @DisplayName("secretaria lava rascunho sem fiel cadastrado, mas não homologa")
    void secretariaLavra_naoHomologa() throws Exception {
        String id = lavrar(UUID.randomUUID().toString().substring(0, 8)).get("id").asString();
        mockMvc.perform(post(BASE + "/" + id + "/homologacao")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("admin não acessa o livro sacramental")
    void adminSemAcesso() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("fluxo: lavrar, homologar, travar edição, averbar, retificar, certidão e histórico")
    void fluxoCompleto() throws Exception {
        String termo = UUID.randomUUID().toString().substring(0, 8);
        String id = lavrar(termo).get("id").asString();

        comoParoco(post(BASE).contentType(MediaType.APPLICATION_JSON).content(teorBatismo(termo)))
                .andExpect(status().isUnprocessableContent());

        comoParoco(post(BASE + "/" + id + "/homologacao"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SEALED"));

        comoParoco(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teor\":{\"livro\":\"9\"}}"))
                .andExpect(status().isConflict());

        MvcResult av = comoParoco(post(BASE + "/" + id + "/averbacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoEvento":"CONFIRMACAO","dataAverbacao":"2026-06-01","numeroDocumento":"N-1",
                                 "teorNota":"Crismada na Paróquia X"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.averbacoes[0].status").value("PENDENTE_HOMOLOGACAO")).andReturn();
        String averbacaoId = objectMapper.readTree(av.getResponse().getContentAsString())
                .get("averbacoes").get(0).get("id").asString();
        comoParoco(post(BASE + "/" + id + "/averbacoes/" + averbacaoId + "/homologacao"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.averbacoes[0].status").value("HOMOLOGADA"));

        comoParoco(post(BASE + "/" + id + "/retificacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campo\":\"nomeHistorico\",\"valorOriginal\":\"Maria da Silva\","
                                + "\"valorCorrigido\":\"Maria da Silva Souza\",\"justificativa\":\"Grafia\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ressalvas[0].campo").value("nomeHistorico"));

        comoParoco(get(BASE + "/" + id + "/certidao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averbacoes[0]").value("Crismada na Paróquia X"))
                .andExpect(jsonPath("$.texto").value(org.hamcrest.Matchers.containsString("Maria da Silva")));

        comoParoco(get(BASE + "/" + id + "/historico"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].acao").value("lavrar"))
                .andExpect(jsonPath("$[0].perfil").value("paroco"));
    }

    @Test
    @DisplayName("livro reservado é invisível à secretaria (404) e o banco veda DELETE")
    void livroReservado_eSemDelete() throws Exception {
        MvcResult r = comoParoco(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(teorBatismo(UUID.randomUUID().toString().substring(0, 8))
                                .replace("\"tipo\":\"BATISMO\"",
                                        "\"tipo\":\"BATISMO\",\"visibilidade\":\"RESERVADO\"")))
                .andExpect(status().isCreated()).andReturn();
        String id = objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asString();

        assertThatThrownBy(() -> jdbc.update("DELETE FROM calendario.assentos_sacramentais WHERE id = ?",
                UUID.fromString(id))).hasMessageContaining("perpetuo");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM calendario.assentos_sacramentais WHERE id = ?",
                Integer.class, UUID.fromString(id))).isEqualTo(1);

        mockMvc.perform(get(BASE + "/" + id).with(user("secretaria").roles("SECRETARIA_PAROQUIAL")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("fila de vínculos pendentes: pároco consulta; secretaria é negada")
    void filaDeVinculosPendentes_soParoco() throws Exception {
        comoParoco(get(BASE + "/vinculos-pendentes")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        mockMvc.perform(get(BASE + "/vinculos-pendentes").with(user("secretaria").roles("SECRETARIA_PAROQUIAL")))
                .andExpect(status().isForbidden());
    }
}
