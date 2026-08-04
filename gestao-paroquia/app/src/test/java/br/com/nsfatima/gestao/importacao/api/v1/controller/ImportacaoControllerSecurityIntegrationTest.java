package br.com.nsfatima.gestao.importacao.api.v1.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class ImportacaoControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminParaBaixarTemplate() throws Exception {
        mockMvc.perform(get("/api/v1/importacao/template?tipo=MEMBRO&formato=CSV"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarUsuarioDeDownloadingTemplate() throws Exception {
        mockMvc.perform(get("/api/v1/importacao/template?tipo=MEMBRO&formato=CSV"))
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
    void deveExigirAuthParaAcessoImportationApi() throws Exception {
        mockMvc.perform(get("/api/v1/importacao/template?tipo=MEMBRO&formato=CSV").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());
    }
}
