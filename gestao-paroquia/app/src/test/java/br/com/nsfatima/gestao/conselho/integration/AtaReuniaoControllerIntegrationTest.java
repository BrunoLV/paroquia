package br.com.nsfatima.gestao.conselho.integration;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno.Vinculo;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarClerigoUseCase;
import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.Mockito;
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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private GerenciarClerigoUseCase clero;

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

    /** Secretário do CPP A com conta de acesso real, ligada ao seu cadastro de fiel (o primeiro membro). */
    private RequestPostProcessor secretarioComConta() {
        UUID usuario = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.usuarios (id, username, password_hash, enabled, roles, fiel_id) "
                + "VALUES (?, ?, 'x', TRUE, 'SECRETARIO_CONSELHO', ?)", usuario, "sec-" + usuario, membrosA.get(0));
        UsuarioDetails detalhes = new UsuarioDetails(usuario, "sec-" + usuario, "x", true, "",
                List.of(new Vinculo(cppA, "CONSELHO", "secretario-conselho")));
        return authentication(new UsernamePasswordAuthenticationToken(detalhes, null, detalhes.getAuthorities()));
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Secretário de Conselho")));
    }

    @Test
    @DisplayName("catálogo de tipos lista só os órgãos em que o usuário redige, pela mesma regra da criação")
    void tiposListamOrgaosDeRedacaoDoUsuario() throws Exception {
        UUID caep = novoConselho("CAEP");
        mockMvc.perform(get(BASE + "/tipos-reuniao").with(secretario(cppA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo=='REUNIAO_CPP')].organizacoesRedacao[0]")
                        .value(org.hamcrest.Matchers.contains(cppA.toString())))
                .andExpect(jsonPath("$[?(@.codigo=='REUNIAO_CAEP')].organizacoesRedacao[0]")
                        .value(org.hamcrest.Matchers.empty()));
        mockMvc.perform(get(BASE + "/tipos-reuniao").with(paroco()))
                .andExpect(jsonPath("$[?(@.codigo=='REUNIAO_CPP')].organizacoesRedacao[0]")
                        .value(org.hamcrest.Matchers.empty()));
        mockMvc.perform(get(BASE + "/tipos-reuniao").with(secretario(caep)))
                .andExpect(jsonPath("$[?(@.codigo=='REUNIAO_CAEP')].organizacoesRedacao[0]")
                        .value(org.hamcrest.Matchers.contains(caep.toString())));
    }

    @Test
    @DisplayName("informa ao redator o seu fiel e a função com que deve constar na presença")
    void presencaDoRedator() throws Exception {
        mockMvc.perform(get(BASE + "/redator-presenca").param("tipo", "REUNIAO_CPP")
                        .param("organizacaoId", cppA.toString()).with(secretarioComConta()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fielId").value(membrosA.get(0).toString()))
                .andExpect(jsonPath("$.funcao").value("Secretário(a)"));
        mockMvc.perform(get(BASE + "/redator-presenca").param("tipo", "REUNIAO_CPP")
                        .param("organizacaoId", cppB.toString()).with(secretarioComConta()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(BASE + "/redator-presenca").param("tipo", "REUNIAO_CPP")
                        .param("organizacaoId", cppA.toString()).with(paroco()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("a ata exige o redator presente com a função de secretário e rejeita funções em conflito")
    void funcoesDaPresenca() throws Exception {
        String semFuncao = corpoCpp(cppA, 3);
        String secretario = membrosA.get(0).toString() + "\",";
        String comFuncao = semFuncao.replace(secretario, secretario + "\"funcao\":\"Secretário(a)\",");

        mockMvc.perform(post(ATAS).with(secretarioComConta()).contentType(MediaType.APPLICATION_JSON)
                        .content(semFuncao))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("redator")));
        mockMvc.perform(post(ATAS).with(secretarioComConta()).contentType(MediaType.APPLICATION_JSON)
                        .content(comFuncao))
                .andExpect(status().isCreated());

        String outro = membrosA.get(1).toString() + "\",";
        mockMvc.perform(post(ATAS).with(secretarioComConta()).contentType(MediaType.APPLICATION_JSON)
                        .content(comFuncao.replace(outro, outro + "\"funcao\":\"Secretário(a)\",")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("uma pessoa")));
        mockMvc.perform(post(ATAS).with(secretarioComConta()).contentType(MediaType.APPLICATION_JSON)
                        .content(comFuncao.replace(outro, outro + "\"funcao\":\"Chefe\",")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("órgão desativado depois do rascunho não deixa a ata avançar, nem ser homologada, mas ainda ser vetada")
    void orgaoDesativadoNaoDeixaAvancar() throws Exception {
        String rascunho = criarRascunho().get("id").asString();
        String pendente = criarPendente().get("id").asString();
        jdbc.update("UPDATE calendario.organizacao SET ativo = FALSE WHERE id = ?", cppA);
        entityManager.clear();

        mockMvc.perform(post(ATAS + "/" + rascunho + "/submissao").with(secretario(cppA)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("inativo")));
        mockMvc.perform(post(ATAS + "/" + pendente + "/homologacao").with(paroco()))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(ATAS + "/" + pendente + "/veto").with(paroco()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Órgão desativado\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("o Pároco, que preside por direito próprio, entra na presença e conta no quórum")
    void parocoPresideEContaNoQuorum() throws Exception {
        UUID paroco = UUID.randomUUID();
        Mockito.when(clero.diretorio()).thenReturn(List.of(new ClerigoInstitucionalResponse(paroco, "Pe. Pedro",
                GrauOrdem.PADRE_DIOCESANO, FuncaoEclesiastica.PAROCO, "NS Fátima")));
        mockMvc.perform(get(BASE + "/clero").with(secretario(cppA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pe. Pedro"))
                .andExpect(jsonPath("$[0].funcao").value("Pároco"))
                .andExpect(jsonPath("$[0].paroco").value(true));
        mockMvc.perform(get(BASE + "/clero").with(paroco())).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get(BASE + "/tipos-reuniao").with(secretario(cppA)))
                .andExpect(jsonPath("$[?(@.codigo=='REUNIAO_CPP')].presidenciaNata")
                        .value(org.hamcrest.Matchers.contains(true)))
                .andExpect(jsonPath("$[?(@.codigo=='ASSEMBLEIA_PAROQUIAL')].presidenciaNata")
                        .value(org.hamcrest.Matchers.contains(false)));

        String semPresidente = corpoCpp(cppA, 3);
        String presidente = "{\"fielId\":\"" + paroco + "\",\"clerigo\":true,\"funcao\":\"Presidente\","
                + "\"situacao\":\"PRESENTE\"},";
        String com = semPresidente.replace("\"nominal\":[", "\"nominal\":[" + presidente);

        for (String corpo : List.of(semPresidente, com)) {
            String criada = mockMvc.perform(post(ATAS).with(secretario(cppA))
                            .contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            String id = objectMapper.readTree(criada).get("id").asString();
            mockMvc.perform(post(ATAS + "/" + id + "/submissao").with(secretario(cppA)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quorum.membrosConvocados").value(6))
                    .andExpect(jsonPath("$.quorum.presentes").value(corpo == com ? 4 : 3))
                    .andExpect(jsonPath("$.quorum.atingido").value(corpo == com));
        }
        mockMvc.perform(post(ATAS).with(secretario(cppA)).contentType(MediaType.APPLICATION_JSON)
                        .content(com.replace(paroco.toString(), UUID.randomUUID().toString())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("clérigo")));
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
        assertThat(pendente.get("quorum").get("minimoNecessario").asInt()).as("5 membros e o Pároco").isEqualTo(4);
        assertThat(pendente.get("quorum").get("presentes").asInt()).isEqualTo(3);
        assertThat(pendente.get("quorum").get("atingido").asBoolean()).isFalse();

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

    @Test
    @DisplayName("exporta ata homologada em PDF como documento oficial, com nome, tipo e sem cache")
    void exportaPdfOficial() throws Exception {
        String id = criarPendente().get("id").asString();
        mockMvc.perform(post(ATAS + "/" + id + "/homologacao").with(paroco())).andExpect(status().isOk());

        MvcResult resultado = mockMvc.perform(get(ATAS + "/" + id + "/exportacao").param("formato", "PDF")
                        .with(secretario(cppA)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(header().string("Content-Disposition", matchesPattern(
                        "attachment; filename=\"ata-001-\\d{4}-reuniao-cpp\\.pdf\".*")))
                .andReturn();

        byte[] pdf = resultado.getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("exporta minuta em Word, narrativa, e o arquivo abre como .docx")
    void exportaMinutaDocx() throws Exception {
        String id = criarPendente().get("id").asString();

        MvcResult resultado = mockMvc.perform(get(ATAS + "/" + id + "/exportacao").param("formato", "DOCX")
                        .param("layout", "NARRATIVA").with(paroco()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("ata-minuta-reuniao-cpp.docx")))
                .andReturn();

        try (XWPFDocument docx = new XWPFDocument(new ByteArrayInputStream(
                resultado.getResponse().getContentAsByteArray()));
             XWPFWordExtractor extrator = new XWPFWordExtractor(docx)) {
            assertThat(extrator.getText()).contains("MINUTA — SEM EFEITO")
                    .contains("realizou-se, em caráter ordinário");
        }
    }

    @Test
    @DisplayName("quem não pode ler a ata não a exporta: 404")
    void exportacaoForaDoEscopo() throws Exception {
        String id = criarRascunho().get("id").asString();

        mockMvc.perform(get(ATAS + "/" + id + "/exportacao").param("formato", "PDF").with(secretario(cppB)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get(ATAS + "/" + id + "/exportacao").param("formato", "PDF")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("cada exportação fica na trilha de auditoria, com formato, layout e situação")
    void exportacaoAuditada() throws Exception {
        String id = criarPendente().get("id").asString();
        mockMvc.perform(get(ATAS + "/" + id + "/exportacao").param("formato", "PDF").with(secretario(cppA)))
                .andExpect(status().isOk());
        mockMvc.perform(get(ATAS + "/" + id + "/exportacao").param("formato", "DOCX").param("conteudo",
                        "SOMENTE_DECISOES").with(paroco()))
                .andExpect(status().isOk());

        mockMvc.perform(get(ATAS + "/" + id + "/historico").with(paroco()))
                .andExpect(jsonPath("$[?(@.acao == 'EXPORTAR_PDF')].ator").value("usuario-secretario-conselho"))
                .andExpect(jsonPath("$[?(@.acao == 'EXPORTAR_DOCX')].ator").value("paroco"));
        String parametros = jdbc.queryForObject("SELECT valores_posteriores FROM calendario.ata_historico "
                + "WHERE ata_id = ?::uuid AND acao = 'EXPORTAR_DOCX'", String.class, id);
        assertThat(parametros).contains("\"conteudo\":\"SOMENTE_DECISOES\"")
                .contains("\"status\":\"PENDENTE_HOMOLOGACAO\"");
    }

    @Test
    @DisplayName("compilação inclui só atas seladas do escopo e responde 404 quando não há nenhuma")
    void compilacao() throws Exception {
        mockMvc.perform(get(ATAS + "/exportacao").param("organizacaoId", cppA.toString()).param("formato", "PDF")
                        .with(paroco()))
                .andExpect(status().isNotFound());

        String id = criarPendente().get("id").asString();
        mockMvc.perform(post(ATAS + "/" + id + "/homologacao").with(paroco())).andExpect(status().isOk());

        mockMvc.perform(get(ATAS + "/exportacao").param("organizacaoId", cppA.toString()).param("formato", "PDF")
                        .with(secretario(cppA)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("livro-de-atas.pdf")));
        mockMvc.perform(get(ATAS + "/exportacao").param("organizacaoId", cppA.toString()).param("formato", "PDF")
                        .with(secretario(cppB)))
                .andExpect(status().isNotFound());
    }
}
