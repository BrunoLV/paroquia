package br.com.nsfatima.gestao.calendario.integration.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UnknownFieldRejectionIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @WithMockUser
        void shouldRejectUnknownFieldsAcrossMigratedEndpoints() throws Exception {
                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-unknown-field-001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("""
                                                {
                                                  "titulo":"Evento",
                                                  "organizacaoResponsavelId":"00000000-0000-0000-0000-0000000000aa",
                                                  "inicio":"2026-06-21T10:00:00Z",
                                                  "fim":"2026-06-21T11:00:00Z",
                                                  "campoExtra":true
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_UNKNOWN_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("campoExtra"));

                mockMvc.perform(post("/api/v1/projetos")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"nome\":\"Projeto\",\"campoExtra\":true}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_UNKNOWN_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("campoExtra"));

                mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(
                                                "{\"tipo\":\"NOTA\",\"conteudo\":\"ok\",\"campoExtra\":true}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_UNKNOWN_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("campoExtra"));

                mockMvc.perform(put("/api/v1/eventos/{eventoId}/envolvidos", "00000000-0000-0000-0000-000000000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"envolvidos\":[],\"campoExtra\":true}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_UNKNOWN_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("campoExtra"));

                mockMvc.perform(put("/api/v1/eventos/{eventoId}/recorrencia", "00000000-0000-0000-0000-000000000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content("{\"frequencia\":\"SEMANAL\",\"intervalo\":1,\"campoExtra\":true}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_UNKNOWN_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("campoExtra"));

                mockMvc.perform(post("/api/v1/aprovacoes")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(
                                                "{\"eventoId\":\"00000000-0000-0000-0000-000000000001\",\"tipoSolicitacao\":\"ALTERACAO_HORARIO\",\"campoExtra\":true}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_UNKNOWN_FIELD"))
                                .andExpect(jsonPath("$.errors[0].field").value("campoExtra"));
        }
}
