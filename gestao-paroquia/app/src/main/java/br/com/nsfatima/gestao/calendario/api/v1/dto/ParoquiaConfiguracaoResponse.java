package br.com.nsfatima.gestao.calendario.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Resposta agregada consolidando dados civis (Matriz), dados canônicos (Governo/Diocese) e
 * a identidade visual da aplicação (cores e logotipos) a partir das fontes únicas da verdade.
 */
@Schema(description = "Configuração e identidade institucional consolidada da paróquia")
public record ParoquiaConfiguracaoResponse(
        @Schema(description = "Identificador da configuração de identidade visual", example = "00000000-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Nome oficial da paróquia (Igreja Matriz)", example = "Paróquia Nossa Senhora de Fátima")
        String nome,

        @Schema(description = "Nome da diocese canônica vinculada", example = "Diocese de São Miguel Paulista")
        String diocese,

        @Schema(description = "Endereço completo da paróquia", example = "Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP")
        String endereco,

        @Schema(description = "CNPJ institucional da paróquia", example = "61.378.766/0001-44")
        String cnpj,

        @Schema(description = "Telefone principal de contato", example = "(11) 2031-0000")
        String telefone,

        @Schema(description = "E-mail institucional", example = "contato@nsfatima.com.br")
        String email,

        @Schema(description = "Cor primária da identidade visual em hexadecimal", example = "#1A237E")
        String corPrimariaHex,

        @Schema(description = "Cor secundária da identidade visual em hexadecimal", example = "#808080")
        String corSecundariaHex,

        @Schema(description = "Indica se o logotipo para fundo claro está cadastrado", example = "true")
        boolean possuiLogoFundoClaro,

        @Schema(description = "Indica se o logotipo para fundo escuro está cadastrado", example = "true")
        boolean possuiLogoFundoEscuro,

        @Schema(description = "URL relativa para download do logotipo para fundo claro", example = "/api/v1/configuracao-paroquia/logo/claro")
        String urlLogoFundoClaro,

        @Schema(description = "URL relativa para download do logotipo para fundo escuro", example = "/api/v1/configuracao-paroquia/logo/escuro")
        String urlLogoFundoEscuro
) {
}
