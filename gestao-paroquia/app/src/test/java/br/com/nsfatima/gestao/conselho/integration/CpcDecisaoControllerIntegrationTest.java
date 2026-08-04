package br.com.nsfatima.gestao.conselho.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class CpcDecisaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create CPC decision as PENDENTE_HOMOLOGACAO")
    void createDecisao_asPendente() throws Exception {
        UUID comunidadeId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/conselhos/decisoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "titulo": "Reforma do Salão Comunitário",
                                  "pautaDescricao": "Aprovação de orçamento para pintura",
                                  "dataReuniao": "%s",
                                  "comunidadeId": "%s"
                                }
                                """, LocalDate.now(), comunidadeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.statusHomologacao").value("PENDENTE_HOMOLOGACAO"));
    }

    @Test
    @WithMockUser(roles = "PASTORAL_VOLUNTARIO_PASTORAL_LOCAL")
    @DisplayName("Non-authorized role should fail to homologate decision")
    void homologarDecisao_forbiddenForUnauthorizedRole() throws Exception {
        UUID decisaoId = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/conselhos/decisoes/" + decisaoId + "/homologacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "HOMOLOGADO"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
