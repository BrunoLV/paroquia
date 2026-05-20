package br.com.nsfatima.gestao.calendario.contract;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class ApprovalPendingContractSupport {

    protected static final MediaType JSON = MediaType.APPLICATION_JSON;

    protected static String createEventoPayload() {
        return """
                {
                  \"titulo\": \"Missa de teste\",
                  \"descricao\": \"Fluxo pendente\",
                  \"organizacaoResponsavelId\": \"00000000-0000-0000-0000-0000000000aa\",
                  \"inicioUtc\": \"2026-12-01T10:00:00Z\",
                  \"fimUtc\": \"2026-12-01T11:00:00Z\",
                  \"status\": \"CONFIRMADO\"
                }
                """;
    }

    protected static String patchEventoPayload() {
        return """
                {
                  \"inicioUtc\": \"2026-12-01T12:00:00Z\",
                  \"fimUtc\": \"2026-12-01T13:00:00Z\"
                }
                """;
    }

    protected static void assertApprovalPending(ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.solicitacaoAprovacaoId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APPROVAL_PENDING"));
    }
}
