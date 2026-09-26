package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Resposta DTO representada por um nó de {@code UnidadeTerritorial}.
 */
public record UnidadeTerritorialResponse(
        UUID id,
        String nome,
        TipoUnidadeTerritorial tipo,
        String contato,
        boolean ativo,
        UUID parentId,

        // Canônicos
        String padroeiro,
        String dataFestaPadroeira,
        LocalDate dataDedicacaoAltar,
        boolean possuiSacrario,
        boolean possuiPiaBatismal,
        UUID clerigoResponsavelId,
        String clerigoResponsavelNome,

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
        String enderecoUf
) {
}
