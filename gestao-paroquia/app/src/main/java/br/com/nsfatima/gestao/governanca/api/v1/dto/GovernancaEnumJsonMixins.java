package br.com.nsfatima.gestao.governanca.api.v1.dto;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.boot.jackson.JacksonMixin;

/**
 * Mix-ins Jackson dos enums do contexto de governanca. O domínio permanece livre de anotações de
 * serialização: o vínculo entre o JSON da API e a fábrica de normalização de cada enum é declarado
 * aqui, na camada {@code api}, e registrado automaticamente pelo Spring Boot.
 */
public interface GovernancaEnumJsonMixins {

    /**
     * Reaplica o criador JSON de {@link AnoParoquialStatus}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(AnoParoquialStatus.class)
    final class AnoParoquialStatusMixin {

        private AnoParoquialStatusMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static AnoParoquialStatus fromJson(String rawValue) {
            return AnoParoquialStatus.fromJson(rawValue);
        }
    }
}
