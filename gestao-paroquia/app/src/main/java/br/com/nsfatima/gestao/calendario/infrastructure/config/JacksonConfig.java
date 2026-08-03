package br.com.nsfatima.gestao.calendario.infrastructure.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

/**
 * Customiza o comportamento de (de)serialização JSON do Jackson para requisições da API de
 * calendário, tornando o binding de payloads mais rígido a fim de rejeitar cedo requisições
 * malformadas em vez de ignorá-las silenciosamente.
 */
@Configuration
public class JacksonConfig {

    /**
     * Habilita falha explícita quando o payload de requisição contém propriedades desconhecidas
     * ou tokens JSON extras após o objeto esperado, evitando que erros de digitação no cliente
     * passem despercebidos.
     *
     * @return customizador que ativa {@code FAIL_ON_UNKNOWN_PROPERTIES} e
     *     {@code FAIL_ON_TRAILING_TOKENS} no {@code JsonMapper} usado pela aplicação
     */
    @Bean
    JsonMapperBuilderCustomizer strictRequestBindingCustomizer() {
        return builder -> builder.enable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }
}
