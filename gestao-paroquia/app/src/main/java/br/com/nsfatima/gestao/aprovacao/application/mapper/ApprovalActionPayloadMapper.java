package br.com.nsfatima.gestao.aprovacao.application.mapper;

import tools.jackson.core.JacksonException;
import br.com.nsfatima.gestao.aprovacao.application.dto.ApprovalActionPayload;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ApprovalActionPayloadMapper {

    private final ObjectMapper objectMapper;

    public ApprovalActionPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(ApprovalActionPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Unable to serialize approval action payload", ex);
        }
    }

    public ApprovalActionPayload toPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return new ApprovalActionPayload(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }
        try {
            return objectMapper.readValue(payloadJson, ApprovalActionPayload.class);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Unable to deserialize approval action payload", ex);
        }
    }
}
