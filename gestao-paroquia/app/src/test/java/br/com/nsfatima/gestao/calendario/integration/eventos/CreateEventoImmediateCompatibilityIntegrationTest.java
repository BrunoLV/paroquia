package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * Regression test — FR-015 compatibility.
 * Roles with IMMEDIATE mode (paroco, conselho coordinator) must still receive
 * 201 CREATED.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CreateEventoImmediateCompatibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    void parocoRoleStillReturns201Created() throws Exception {
        long countBefore = eventoJpaRepository.count();

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Missa do paroco - imediato",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-09-01T08:00:00Z",
                                  "fim": "2027-09-01T09:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Missa do paroco - imediato"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        assertThat(eventoJpaRepository.count()).isEqualTo(countBefore + 1);
    }

    @Test
    void bispoDiocesanoRoleStillReturns201Created() throws Exception {
        long countBefore = eventoJpaRepository.count();

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "bispo-diocesano")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Evento bispo - imediato",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-09-02T10:00:00Z",
                                  "fim": "2027-09-02T11:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Evento bispo - imediato"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        assertThat(eventoJpaRepository.count()).isEqualTo(countBefore + 1);
    }

    @Test
    void secretarioConselhoRoleRoutesToApprovalWith202Accepted() throws Exception {
        long countBefore = eventoJpaRepository.count();

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "secretario-conselho")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Evento conselho - requer aprovacao",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-09-03T14:00:00Z",
                                  "fim": "2027-09-03T15:00:00Z",
                                  "adicionadoExtraJustificativa": "Justificativa formal do conselho"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.solicitacaoAprovacaoId").isNotEmpty());

        assertThat(eventoJpaRepository.count()).isEqualTo(countBefore);
    }
}
