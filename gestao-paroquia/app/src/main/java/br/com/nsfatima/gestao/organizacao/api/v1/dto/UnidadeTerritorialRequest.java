package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload de requisição para criação ou atualização de {@code UnidadeTerritorial}.
 */
public record UnidadeTerritorialRequest(
        @NotBlank(message = "O nome da unidade territorial é obrigatório")
        String nome,

        @NotNull(message = "O tipo da unidade territorial é obrigatório")
        TipoUnidadeTerritorial tipo,

        String contato,

        boolean ativo,

        UUID parentId,

        // Canônicos
        String padroeiro,
        String dataFestaPadroeira,
        LocalDate dataDedicacaoAltar,
        Boolean possuiSacrario,
        Boolean possuiPiaBatismal,
        UUID clerigoResponsavelId,

        // Fiscais e Jurídicos
        String cnpj,
        String inscricaoMunicipal,
        String inscricaoImobiliariaIptu,
        String processoImunidadeIptu,

        // Registro de Imóveis (RGI)
        String cartorioRgi,
        String matriculaImovel,
        String tipoAquisicao,
        BigDecimal areaTerrenoM2,
        BigDecimal areaConstruidaM2,

        // Segurança e Concessionárias
        String avcbNumero,
        LocalDate avcbValidade,
        Integer lotacaoMaximaBombeiros,
        String codigoConsumidorEnergia,
        String codigoConsumidorAgua,

        // Contato e Endereço Estruturado
        String telefone,
        String email,
        String enderecoCep,
        String enderecoLogradouro,
        String enderecoNumero,
        String enderecoComplemento,
        String enderecoBairro,
        String enderecoCidade,
        @Size(max = 2, message = "UF deve ter no máximo 2 caracteres")
        String enderecoUf
) {
}
