package br.com.nsfatima.gestao.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@SpringBootTest
@AutoConfigureMockMvc
class EventosCreatePendenteContractTest extends AprovacaoPendenteContractSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createEventoWithSecretarioReturns202WithPendenteBody() throws Exception {
        String payload = """
                {
                  "titulo": "Contrato pendente aprovacao",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                  "inicio": "2027-07-15T10:00:00Z",
                  "fim": "2027-07-15T11:00:00Z",
                  "adicionadoExtraJustificativa": "Justificativa formal do secretario"
                }
                """;

        assertAprovacaoPendente(
                mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "secretario-conselho")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(JSON)
                        .content(payload)));
    }

    @Test
    void createEventoPendenteResponseHasRequiredFields() throws Exception {
        String payload = """
                {
                  "titulo": "Campos obrigatorios pendente",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                  "inicio": "2027-08-01T09:00:00Z",
                  "fim": "2027-08-01T10:00:00Z"
                }
                """;

        assertAprovacaoPendente(
                mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "vigario")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(JSON)
                        .content(payload)));
    }
}
