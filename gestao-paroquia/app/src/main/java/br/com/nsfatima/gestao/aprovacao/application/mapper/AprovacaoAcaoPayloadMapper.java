package br.com.nsfatima.gestao.aprovacao.application.mapper;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class AprovacaoAcaoPayloadMapper {

    private final ObjectMapper objectMapper;

    public AprovacaoAcaoPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(AprovacaoAcaoPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Unable to serialize approval action payload", ex);
        }
    }

    public AprovacaoAcaoPayload toPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return new AprovacaoAcaoPayload(null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null);

        }
        try {
            return objectMapper.readValue(payloadJson, AprovacaoAcaoPayload.class);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Unable to deserialize approval action payload", ex);
        }
    }
}
