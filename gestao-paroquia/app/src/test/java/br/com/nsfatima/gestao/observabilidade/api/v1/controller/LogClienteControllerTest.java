package br.com.nsfatima.gestao.observabilidade.api.v1.controller;

import br.com.nsfatima.gestao.observabilidade.api.v1.dto.LoteLogClienteRequest;
import br.com.nsfatima.gestao.observabilidade.api.v1.dto.LogClienteDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogClienteControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        LogClienteController controller = new LogClienteController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Deve aceitar lote de logs valido e retornar 202 Accepted")
    void deveAceitarLoteDeLogsValido() throws Exception {
        LogClienteDTO log1 = new LogClienteDTO(
                "INFO", "Usuário acessou a página de eventos", "/calendario", "Calendario", null, Instant.now()
        );
        LogClienteDTO log2 = new LogClienteDTO(
                "ERROR", "Erro de renderizacao", "/fieis", "FielModal", "TypeError: null", Instant.now()
        );
        LoteLogClienteRequest request = new LoteLogClienteRequest(List.of(log1, log2));

        mockMvc.perform(post("/api/v1/observabilidade/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("Deve sanitizar PII em logs de erro antes de processar")
    void deveSanitizarPiiEmLogs() {
        LogClienteDTO logComCpfEEmail = new LogClienteDTO(
                "WARN",
                "Tentativa de cadastro com CPF 123.456.789-01 e email padre@paroquia.org",
                "/fieis",
                "FielForm",
                "Stack: erro com 12345678901 e contato@teste.com",
                Instant.now()
        );

        assertThat(logComCpfEEmail.mensagem()).doesNotContain("123.456.789-01");
        assertThat(logComCpfEEmail.mensagem()).doesNotContain("padre@paroquia.org");
        assertThat(logComCpfEEmail.mensagem()).contains("***.***.***-**");
        assertThat(logComCpfEEmail.mensagem()).contains("*****@*****.***");

        assertThat(logComCpfEEmail.detalhes()).doesNotContain("contato@teste.com");
    }

    @Test
    @DisplayName("Deve rejeitar lote com corpo vazio")
    void deveRejeitarLoteVazio() throws Exception {
        mockMvc.perform(post("/api/v1/observabilidade/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"logs\": []}"))
                .andExpect(status().isBadRequest());
    }
}
