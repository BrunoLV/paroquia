package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.boot.jackson.JacksonMixin;

/**
 * Mix-ins Jackson dos enums do contexto de dashboard. O domínio permanece livre de anotações de
 * serialização: o vínculo entre o JSON da API e a fábrica de normalização de cada enum é declarado
 * aqui, na camada {@code api}, e registrado automaticamente pelo Spring Boot.
 */
public interface DashboardEnumJsonMixins {

    /**
     * Reaplica o criador JSON de {@link PeriodoDashboard}: a fábrica de domínio {@code fromJson} normaliza o texto
     * recebido (caixa, espaços, aliases), preservando o contrato de desserialização das requisições.
     */
    @JacksonMixin(PeriodoDashboard.class)
    final class PeriodoDashboardMixin {

        private PeriodoDashboardMixin() {
        }

        /**
         * Delega à fábrica do próprio enum; o corpo é ignorado pelo Jackson, que só lê a anotação.
         *
         * @param rawValue texto bruto recebido no JSON
         * @return a constante normalizada
         */
        @JsonCreator
        public static PeriodoDashboard fromJson(String rawValue) {
            return PeriodoDashboard.fromJson(rawValue);
        }
    }
}
