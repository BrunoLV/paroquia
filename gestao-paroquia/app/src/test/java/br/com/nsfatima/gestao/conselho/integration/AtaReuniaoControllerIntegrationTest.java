package br.com.nsfatima.gestao.conselho.integration;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno.Vinculo;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo das atas pela API, com PostgreSQL real: papéis escopados por órgão, ciclo de vida, selagem,
 * numeração, escopo de leitura e trilha de auditoria.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AtaReuniaoControllerIntegrationTest {

    private static final String BASE = "/api/v1/conselhos";
    private static final String ATAS = BASE + "/atas";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbc;

    private UUID cppA;
    private UUID cppB;
    private final List<UUID> membrosA = new ArrayList<>();

    @BeforeEach
    void cenario() {
        cppA = novoConselho("CPP");
        cppB = novoConselho("CPP");
        for (int i = 0; i < 5; i++) {
            membrosA.add(novoMembro(cppA, i == 0 ? "secretario-conselho" : "membro-conselho-pastoral"));
        }
    }

    private UUID novoConselho(String subtipo) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.organizacao (id, nome, tipo, subtipo_conselho) "
                + "VALUES (?, ?, 'CONSELHO', ?)", id, "Conselho " + id, subtipo);
        return id;
    }

    private UUID novoMembro(UUID organizacao, String papel) {
        UUID fiel = UUID.randomUUID();
        String digitos = fiel.toString().replaceAll("[^0-9]", "0");
        String cpf = (digitos + "00000000000").substring(0, 11);
        jdbc.update("INSERT INTO calendario.fieis (id, nome, cpf, cpf_hash, data_nascimento, email, telefone, "
                        + "endereco_logradouro, vocacao, ativo, criado_em_utc, atualizado_em_utc, criado_por, "
                        + "atualizado_por) VALUES (?, ?, ?, ?, '1990-01-01', ?, '11911111111', 'Rua 1', 'LEIGO', "
                        + "TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'teste', 'teste')",
                fiel, "Fiel " + fiel, cpf, fiel.toString().replace("-", "") + fiel.toString().replace("-", ""),
                fiel + "@example.com");
        jdbc.update("INSERT INTO calendario.membro_grupo (id, fiel_id, organizacao_id, tipo_organizacao, papel, "
                        + "ativo, criado_em_utc, atualizado_em_utc) VALUES (?, ?, ?, 'CONSELHO', ?, TRUE, "
                        + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", UUID.randomUUID(), fiel, organizacao, papel);
        return fiel;
    }

    private static RequestPostProcessor vinculado(UUID organizacao, String papel) {
        UsuarioDetails detalhes = new UsuarioDetails(UUID.randomUUID(), "usuario-" + papel, "x", true, "",
                List.of(new Vinculo(organizacao, "CONSELHO", papel)));
        return authentication(new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities()));
    }

    private static RequestPostProcessor secretario(UUID organizacao) {
        return vinculado(organizacao, "secretario-conselho");
    }

    private static RequestPostProcessor paroco() {
        return user("paroco").roles("PAROCO");
    }

    private String corpoCpp(UUID organizacao, int presentes) {
        StringBuilder nominal = new StringBuilder();
        for (int i = 0; i < presentes; i++) {
            nominal.append(i == 0 ? "" : ",")
                    .append("{\"fielId\":\"").append(membrosA.get(i)).append("\",\"situacao\":\"PRESENTE\"}");
        }
        return """
                {"tipo":"REUNIAO_CPP","organizacaoConvocanteId":"%s","conteudo":{
                  "titulo":"Reunião de junho","dataReuniao":"2026-06-01","local":"Salão paroquial",
                  "carater":"ORDINARIA","modalidade":"PRESENCIAL",
                  "presenca":{"nominal":[%s]},
                  "ataAnterior":{"aprovacao":"NAO_APLICAVEL"},
                  "itens":[{"titulo":"Reforma do telhado","relato":"Discutiu-se o orçamento.",
                    "deliberacoes":[{"texto":"Aprovar o orçamento","resultado":"PARECER_FAVORAVEL",
                      "votacao":{"favor":2,"contra":0,"abstencoes":0}}]}]}}
                """.formatted(organizacao, nominal);
    }

    private String conteudoValido() {
        return objectMapper.writeValueAsString(objectMapper.readTree(corpoCpp(cppA, 3)).get("conteudo"));
    }

    private JsonNode criarRascunho() throws Exception {
        MvcResult r = mockMvc.perform(post(ATAS).with(secretario(cppA)).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCpp(cppA, 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString());
    }

    private JsonNode criarPendente() throws Exception {
        String id = criarRascunho().get("id").asString();
        mockMvc.perform(post(ATAS + "/" + id + "/submissao").with(secretario(cppA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE_HOMOLOGACAO"));
        return objectMapper.readTree(mockMvc.perform(get(ATAS + "/" + id).with(paroco()))
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    @DisplayName("secretário do órgão abre o rascunho, sem número e com ações de redação")
    void secretarioAbreRascunho() throws Exception {
        JsonNode ata = criarRascunho();

        assertThat(ata.get("numero").isNull()).isTrue();
        assertThat(ata.get("redator").asString()).startsWith("usuario-");
        assertThat(ata.get("acoes").toString()).contains("REDIGIR", "SUBMETER").doesNotContain("HOMOLOGAR");
        assertThat(ata.get("conteudo").get("presencas").get(0).get("nome").asString()).startsWith("Fiel ");
    }

    @Test
    @DisplayName("secretário de outro órgão não abre ata neste órgão")
    void outroOrgaoNaoAbre() throws Exception {
        mockMvc.perform(post(ATAS).with(secretario(cppB)).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCpp(cppA, 3)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Pároco não redige: só homologa e veta")
    void parocoNaoRedige() throws Exception {
        mockMvc.perform(post(ATAS).with(paroco()).contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCpp(cppA, 3)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("presença fora dos membros do órgão é rejeitada")
    void presencaDeNaoMembro() throws Exception {
        UUID estranho = novoMembro(cppB, "membro-conselho-pastoral");
        String corpo = corpoCpp(cppA, 1).replace(membrosA.get(0).toString(), estranho.toString());

        mockMvc.perform(post(ATAS).with(secretario(cppA)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("membro lê a ata do seu órgão mas não a edita")
    void membroLeNaoEdita() throws Exception {
        String id = criarRascunho().get("id").asString();
        RequestPostProcessor membro = vinculado(cppA, "membro-conselho-pastoral");

        mockMvc.perform(get(ATAS + "/" + id).with(membro)).andExpect(status().isOk());
        mockMvc.perform(put(ATAS + "/" + id).with(membro).contentType(MediaType.APPLICATION_JSON)
                        .content(conteudoValido()))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(ATAS + "/" + id + "/submissao").with(membro)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("quem não é do órgão e não é clero recebe 404 e lista vazia; ADMIN também")
    void escopoDeLeitura() throws Exception {
        String id = criarRascunho().get("id").asString();

        mockMvc.perform(get(ATAS + "/" + id).with(secretario(cppB))).andExpect(status().isNotFound());
        mockMvc.perform(get(ATAS + "/" + id).with(user("admin").roles("ADMIN"))).andExpect(status().isNotFound());
        mockMvc.perform(get(ATAS).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        mockMvc.perform(get(ATAS).with(secretario(cppB)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        mockMvc.perform(get(ATAS).with(secretario(cppA)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @DisplayName("Pároco homologa: sela, numera, apura o quórum e bloqueia a edição")
    void parocoHomologa() throws Exception {
        JsonNode pendente = criarPendente();
        String id = pendente.get("id").asString();
        assertThat(pendente.get("quorum").get("minimoNecessario").asInt()).isEqualTo(3);
        assertThat(pendente.get("quorum").get("presentes").asInt()).isEqualTo(3);
        assertThat(pendente.get("quorum").get("atingido").asBoolean()).isTrue();

        mockMvc.perform(post(ATAS + "/" + id + "/homologacao").with(secretario(cppA)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(ATAS + "/" + id + "/homologacao").with(paroco()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HOMOLOGADA"))
                .andExpect(jsonPath("$.homologador").value("paroco"))
                .andExpect(jsonPath("$.numero").value(startsWith("001/")));

        mockMvc.perform(put(ATAS + "/" + id).with(secretario(cppA)).contentType(MediaType.APPLICATION_JSON)
                        .content(conteudoValido()))
                .andExpect(status().isConflict());
        mockMvc.perform(post(ATAS + "/" + id + "/homologacao").with(paroco())).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("veto exige motivo; a ata vetada volta ao redator com o motivo à vista")
    void vetoEReabertura() throws Exception {
        String id = criarPendente().get("id").asString();

        mockMvc.perform(post(ATAS + "/" + id + "/veto").with(paroco()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\" \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(ATAS + "/" + id + "/veto").with(paroco()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Falta o orçamento detalhado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VETADA"));
        mockMvc.perform(post(ATAS + "/" + id + "/reabertura").with(secretario(cppA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.motivoVeto").value("Falta o orçamento detalhado"));
    }

    @Test
    @DisplayName("ata retificadora nasce da homologada, em rascunho, ligada à original")
    void retificacao() throws Exception {
        String id = criarPendente().get("id").asString();
        mockMvc.perform(post(ATAS + "/" + id + "/homologacao").with(paroco())).andExpect(status().isOk());

        mockMvc.perform(post(ATAS + "/" + id + "/retificacao").with(secretario(cppA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"justificativa\":\"Erro de grafia\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.ataRetificadaId").value(id))
                .andExpect(jsonPath("$.justificativaRetificacao").value("Erro de grafia"));
    }

    @Test
    @DisplayName("a trilha é visível ao Pároco, não ao secretário, e registra cada ação")
    void trilha() throws Exception {
        String id = criarPendente().get("id").asString();
        mockMvc.perform(post(ATAS + "/" + id + "/homologacao").with(paroco())).andExpect(status().isOk());

        mockMvc.perform(get(ATAS + "/" + id + "/historico").with(secretario(cppA))).andExpect(status().isForbidden());
        mockMvc.perform(get(ATAS + "/" + id + "/historico").with(paroco()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].acao").value("RASCUNHAR"))
                .andExpect(jsonPath("$[1].acao").value("SUBMETER"))
                .andExpect(jsonPath("$[2].acao").value("HOMOLOGAR"))
                .andExpect(jsonPath("$[2].ator").value("paroco"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM calendario.auditoria_operacoes WHERE recurso_id = ? "
                + "AND recurso_tipo = 'ATA_REUNIAO'", Integer.class, id)).isEqualTo(3);
    }

    @Test
    @DisplayName("ata inexistente responde 404, nunca 500")
    void inexistente() throws Exception {
        mockMvc.perform(post(ATAS + "/" + UUID.randomUUID() + "/homologacao").with(paroco()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("catálogo de tipos expõe as 12 reuniões com suas regras")
    void tipos() throws Exception {
        mockMvc.perform(get(BASE + "/tipos-reuniao").with(secretario(cppA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[0].codigo").value("REUNIAO_CPP"))
                .andExpect(jsonPath("$[0].exigeHomologacao").value(true));
    }
}
