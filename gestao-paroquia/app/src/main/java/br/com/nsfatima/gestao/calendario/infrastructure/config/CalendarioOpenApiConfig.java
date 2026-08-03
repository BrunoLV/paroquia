package br.com.nsfatima.gestao.calendario.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura os metadados exibidos na documentação OpenAPI/Swagger UI para a API de calendário,
 * definindo título, versão e descrição apresentados aos consumidores da API.
 */
@Configuration
public class CalendarioOpenApiConfig {

    /**
     * Constrói a descrição OpenAPI customizada com as informações de título e versão da API
     * de gerenciamento de eventos e projetos da paróquia.
     *
     * @return instância de {@link OpenAPI} com as informações customizadas
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Paróquia NS Fátima - Calendário API")
                        .version("1.0.0")
                        .description("API para gerenciamento de eventos e projetos da paróquia."));
    }
}
