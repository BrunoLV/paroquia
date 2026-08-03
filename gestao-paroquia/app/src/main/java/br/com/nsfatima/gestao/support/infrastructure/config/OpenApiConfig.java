package br.com.nsfatima.gestao.support.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central do OpenAPI/Swagger da API, definindo metadados gerais da documentação
 * (título, versão, descrição) e o esquema de segurança baseado em cookie de sessão
 * ({@code JSESSIONID}) usado pela autenticação da aplicação — necessário para que o Swagger UI
 * consiga representar corretamente endpoints protegidos e permitir testes autenticados a partir
 * da própria documentação.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gestão Paróquia API",
                version = "1.0",
                description = "API para o sistema de gestão da Paróquia Nossa Senhora de Fátima"
        ),
        security = @SecurityRequirement(name = "cookieAuth")
)
@SecurityScheme(
        name = "cookieAuth",
        type = SecuritySchemeType.APIKEY,
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.COOKIE,
        paramName = "JSESSIONID"
)
public class OpenApiConfig {
}
