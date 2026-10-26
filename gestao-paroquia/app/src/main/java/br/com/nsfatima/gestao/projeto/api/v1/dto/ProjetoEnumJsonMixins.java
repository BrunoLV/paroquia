package br.com.nsfatima.gestao.projeto.api.v1.dto;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.boot.jackson.JacksonMixin;

/**
 * Mix-ins Jackson dos enums do contexto de projeto. O domínio permanece livre de anotações de
 * serialização: o vínculo entre o JSON da API e a fábrica de normalização de cada enum é declarado
 * aqui, na camada {@code api}, e registrado automaticamente pelo Spring Boot.
 */
public interface ProjetoEnumJsonMixins {

    /**
     * Reaplica o criador JSON de {@link ProjetoStatus}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(ProjetoStatus.class)
    final class ProjetoStatusMixin {

        private ProjetoStatusMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static ProjetoStatus fromJson(String rawValue) {
            return ProjetoStatus.fromJson(rawValue);
        }
    }
}
