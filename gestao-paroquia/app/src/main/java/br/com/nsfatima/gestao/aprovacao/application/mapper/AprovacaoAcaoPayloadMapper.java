package br.com.nsfatima.gestao.aprovacao.application.mapper;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Serializa/desserializa o {@link AprovacaoAcaoPayload} para/do formato JSON persistido em
 * {@code AprovacaoEntity#actionPayloadJson}. Permite que a ação de negócio pendente de uma solicitação de
 * aprovação (dados do evento a criar/atualizar/cancelar) sobreviva entre o momento da solicitação (PENDENTE) e
 * o momento da decisão, quando é lida de volta pela {@code EstrategiaExecucaoAprovacao} correspondente para
 * efetivar a ação.
 */
@Component
public class AprovacaoAcaoPayloadMapper {

    private final ObjectMapper objectMapper;

    public AprovacaoAcaoPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Serializa o payload da ação pendente em JSON para persistência.
     *
     * @param payload payload da ação de negócio associada à solicitação de aprovação
     * @return representação JSON do payload
     * @throws IllegalArgumentException se o payload não puder ser serializado
     */
    public String toJson(AprovacaoAcaoPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Unable to serialize approval action payload", ex);
        }
    }

    /**
     * Desserializa o JSON persistido de volta para o payload da ação pendente. Um JSON nulo/vazio (aprovações
     * legadas sem payload) resulta em um payload com todos os campos nulos, em vez de falhar.
     *
     * @param payloadJson JSON persistido do payload da ação, podendo ser {@code null} ou vazio
     * @return payload reconstruído da ação de negócio
     * @throws IllegalArgumentException se o JSON não puder ser desserializado
     */
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
