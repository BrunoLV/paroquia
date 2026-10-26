package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.boot.jackson.JacksonMixin;

/**
 * Mix-ins Jackson dos enums do contexto de organizacao. O domínio permanece livre de anotações de
 * serialização: o vínculo entre o JSON da API e a fábrica de normalização de cada enum é declarado
 * aqui, na camada {@code api}, e registrado automaticamente pelo Spring Boot.
 */
public interface OrganizacaoEnumJsonMixins {

    /**
     * Reaplica o criador JSON de {@link SetorPastoral}: a fábrica de domínio {@code fromValue} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(SetorPastoral.class)
    final class SetorPastoralMixin {

        private SetorPastoralMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static SetorPastoral fromValue(String rawValue) {
            return SetorPastoral.fromValue(rawValue);
        }
    }

    /**
     * Reaplica o criador JSON de {@link TipoOrganizacao}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(TipoOrganizacao.class)
    final class TipoOrganizacaoMixin {

        private TipoOrganizacaoMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static TipoOrganizacao fromJson(String rawValue) {
            return TipoOrganizacao.fromJson(rawValue);
        }
    }
}
