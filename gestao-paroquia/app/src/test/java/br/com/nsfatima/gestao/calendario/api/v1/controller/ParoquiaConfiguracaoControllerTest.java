package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.AtualizarIdentidadeVisualRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.AtualizarIdentidadeVisualUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.AtualizarLogoParoquiaUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.ObterConfiguracaoParoquiaUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.ObterLogoParoquiaUseCase;
import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ParoquiaConfiguracaoController - Testes de Integração")
class ParoquiaConfiguracaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;

    @MockitoBean
    private AtualizarIdentidadeVisualUseCase atualizarIdentidadeVisualUseCase;

    @MockitoBean
    private AtualizarLogoParoquiaUseCase atualizarLogoParoquiaUseCase;

    @MockitoBean
    private ObterLogoParoquiaUseCase obterLogoParoquiaUseCase;

    @Test
    @DisplayName("GET /api/v1/configuracao-paroquia deve ser acessível sem autenticação")
    void deveObterConfiguracaoSemAutenticacao() throws Exception {
        ParoquiaConfiguracaoResponse response = new ParoquiaConfiguracaoResponse(
                UUID.randomUUID(),
                "Paróquia Nossa Senhora de Fátima",
                "Diocese de São Miguel Paulista",
                "Rua Gijoca, 7",
                "61.378.766/0001-44",
                "(11) 2031-0000",
                "contato@nsfatima.com.br",
                "#1A237E",
                "#808080",
                true,
                true,
                "/api/v1/configuracao-paroquia/logo/claro",
                "/api/v1/configuracao-paroquia/logo/escuro"
        );
        when(obterConfiguracaoParoquiaUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/api/v1/configuracao-paroquia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Paróquia Nossa Senhora de Fátima"))
                .andExpect(jsonPath("$.diocese").value("Diocese de São Miguel Paulista"))
                .andExpect(jsonPath("$.corPrimariaHex").value("#1A237E"))
                .andExpect(jsonPath("$.possuiLogoFundoClaro").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/configuracao-paroquia/logo/claro deve servir bytes do logotipo sem autenticação")
    void deveObterLogoClaroSemAutenticacao() throws Exception {
        byte[] fakeLogo = new byte[]{10, 20, 30};
        when(obterLogoParoquiaUseCase.execute(VarianteFundoLogo.CLARO))
                .thenReturn(Optional.of(new ObterLogoParoquiaUseCase.LogoBinario(fakeLogo, "image/png")));

        mockMvc.perform(get("/api/v1/configuracao-paroquia/logo/claro"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().exists("Cache-Control"))
                .andExpect(content().bytes(fakeLogo));
    }

    @Test
    @DisplayName("GET /api/v1/configuracao-paroquia/logo/escuro deve retornar 404 quando inexistente")
    void deveRetornar404QuandoLogoNaoExiste() throws Exception {
        when(obterLogoParoquiaUseCase.execute(VarianteFundoLogo.ESCURO))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/configuracao-paroquia/logo/escuro"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/configuracao-paroquia deve rejeitar requisição sem autenticação")
    void deveRejeitarAtualizacaoSemAutenticacao() throws Exception {
        AtualizarIdentidadeVisualRequest request = new AtualizarIdentidadeVisualRequest("#112233", "#445566");

        mockMvc.perform(put("/api/v1/configuracao-paroquia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/configuracao-paroquia deve permitir atualização com perfil ADMIN")
    void devePermitirAtualizacaoComAdmin() throws Exception {
        AtualizarIdentidadeVisualRequest request = new AtualizarIdentidadeVisualRequest("#112233", "#445566");
        ParoquiaConfiguracaoResponse response = new ParoquiaConfiguracaoResponse(
                UUID.randomUUID(), "Paróquia", "Diocese", "Endereço", null, null, null,
                "#112233", "#445566", true, true, "/logo/claro", "/logo/escuro"
        );
        when(atualizarIdentidadeVisualUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/configuracao-paroquia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.corPrimariaHex").value("#112233"));
    }

    @Test
    @WithMockUser(roles = "PAROCO")
    @DisplayName("PUT /api/v1/configuracao-paroquia deve permitir atualização com perfil PAROCO")
    void devePermitirAtualizacaoComParoco() throws Exception {
        AtualizarIdentidadeVisualRequest request = new AtualizarIdentidadeVisualRequest("#112233", "#445566");
        ParoquiaConfiguracaoResponse response = new ParoquiaConfiguracaoResponse(
                UUID.randomUUID(), "Paróquia", "Diocese", "Endereço", null, null, null,
                "#112233", "#445566", true, true, "/logo/claro", "/logo/escuro"
        );
        when(atualizarIdentidadeVisualUseCase.execute(any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/configuracao-paroquia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.corPrimariaHex").value("#112233"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/v1/configuracao-paroquia/logo/claro deve permitir upload multipart com ADMIN")
    void devePermitirUploadMultipartComAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "logo.png", "image/png", new byte[]{1, 2, 3, 4}
        );

        ParoquiaConfiguracaoResponse response = new ParoquiaConfiguracaoResponse(
                UUID.randomUUID(), "Paróquia", "Diocese", "Endereço", null, null, null,
                "#1A237E", "#808080", true, false, "/logo/claro", "/logo/escuro"
        );
        when(atualizarLogoParoquiaUseCase.execute(eq(VarianteFundoLogo.CLARO), any(), eq("image/png")))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/configuracao-paroquia/logo/claro")
                        .file(file)
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.possuiLogoFundoClaro").value(true));
    }
}
