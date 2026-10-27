package br.com.nsfatima.gestao.calendario.api.v1.dto;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.FrequenciaRecorrenciaInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.boot.jackson.JacksonMixin;

/**
 * Mix-ins Jackson dos enums do contexto de calendario. O domínio permanece livre de anotações de
 * serialização: o vínculo entre o JSON da API e a fábrica de normalização de cada enum é declarado
 * aqui, na camada {@code api}, e registrado automaticamente pelo Spring Boot.
 */
public interface CalendarioEnumJsonMixins {

    /**
     * Reaplica o criador JSON de {@link CategoriaEvento}: a fábrica de domínio {@code fromValue} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(CategoriaEvento.class)
    final class CategoriaEventoMixin {

        private CategoriaEventoMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static CategoriaEvento fromValue(String rawValue) {
            return CategoriaEvento.fromValue(rawValue);
        }
    }

    /**
     * Reaplica o criador JSON de {@link EventoStatusInput}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(EventoStatusInput.class)
    final class EventoStatusInputMixin {

        private EventoStatusInputMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static EventoStatusInput fromJson(String rawValue) {
            return EventoStatusInput.fromJson(rawValue);
        }
    }

    /**
     * Reaplica o criador JSON de {@link FormatoEvento}: a fábrica de domínio {@code fromValue} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(FormatoEvento.class)
    final class FormatoEventoMixin {

        private FormatoEventoMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static FormatoEvento fromValue(String rawValue) {
            return FormatoEvento.fromValue(rawValue);
        }
    }

    /**
     * Reaplica o criador JSON de {@link FrequenciaRecorrenciaInput}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(FrequenciaRecorrenciaInput.class)
    final class FrequenciaRecorrenciaInputMixin {

        private FrequenciaRecorrenciaInputMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static FrequenciaRecorrenciaInput fromJson(String rawValue) {
            return FrequenciaRecorrenciaInput.fromJson(rawValue);
        }
    }

    /**
     * Reaplica o criador JSON de {@link TipoObservacaoInput}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(TipoObservacaoInput.class)
    final class TipoObservacaoInputMixin {

        private TipoObservacaoInputMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static TipoObservacaoInput fromJson(String rawValue) {
            return TipoObservacaoInput.fromJson(rawValue);
        }
    }

    /**
     * Reaplica o criador JSON de {@link TipoSolicitacaoInput}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(TipoSolicitacaoInput.class)
    final class TipoSolicitacaoInputMixin {

        private TipoSolicitacaoInputMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static TipoSolicitacaoInput fromJson(String rawValue) {
            return TipoSolicitacaoInput.fromJson(rawValue);
        }
    }
}
