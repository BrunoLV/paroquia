package br.com.nsfatima.gestao.importacao.api.v1.controller;

import br.com.nsfatima.gestao.importacao.application.service.LeitorArquivoImportacao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class ImportacaoControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminParaBaixarModelo() throws Exception {
        mockMvc.perform(get("/api/v1/importacao/modelo?tipo=MEMBRO&formato=CSV"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarUsuarioDeDownloadingTemplate() throws Exception {
        mockMvc.perform(get("/api/v1/importacao/modelo?tipo=MEMBRO&formato=CSV"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminParaValidarArquivo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "membros.csv", "text/csv",
                "nomeCompleto,email\nJoão,joao@email.com".getBytes(StandardCharsets.UTF_8)

        );

        mockMvc.perform(multipart("/api/v1/importacao/validar?tipo=MEMBRO").file(file))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarUsuarioDeValidatingArquivo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "membros.csv", "text/csv",
                "nomeCompleto,email\nJoão,joao@email.com".getBytes(StandardCharsets.UTF_8)

        );

        mockMvc.perform(multipart("/api/v1/importacao/validar?tipo=MEMBRO").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRejeitarConfirmacaoSemItensOuAcimaDoLimiteDeLinhas() throws Exception {
        mockMvc.perform(post("/api/v1/importacao/confirmar?tipo=MEMBRO")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itens\":[]}"))
                .andExpect(status().isBadRequest());

        String itens = IntStream.rangeClosed(1, LeitorArquivoImportacao.MAX_LINHAS + 1)
                .mapToObj(i -> "{\"linha\":" + i + ",\"dados\":{}}").collect(Collectors.joining(","));
        mockMvc.perform(post("/api/v1/importacao/confirmar?tipo=MEMBRO")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itens\":[" + itens + "]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveDevolverErroPorLinhaNaConfirmacaoEmVezDeFalharOLote() throws Exception {
        mockMvc.perform(post("/api/v1/importacao/confirmar?tipo=MEMBRO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[{\"linha\":2,\"dados\":{\"usuarioUsername\":\"inexistente\"}}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrosImportados").value(0))
                .andExpect(jsonPath("$.erros[0].linha").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarConfirmacaoParaPapelSemPermissao() throws Exception {
        mockMvc.perform(post("/api/v1/importacao/confirmar?tipo=MEMBRO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itens\":[{\"linha\":2,\"dados\":{}}]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveExigirAutenticacaoParaConfirmar() throws Exception {
        mockMvc.perform(post("/api/v1/importacao/confirmar?tipo=MEMBRO").header("X-Test-Anonymous", "true")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itens\":[]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveExigirAuthParaAcessoImportationApi() throws Exception {
        mockMvc.perform(get("/api/v1/importacao/modelo?tipo=MEMBRO&formato=CSV").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());
    }
}
