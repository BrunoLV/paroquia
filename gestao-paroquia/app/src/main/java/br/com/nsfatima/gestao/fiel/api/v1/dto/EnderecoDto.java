package br.com.nsfatima.gestao.fiel.api.v1.dto;

public record EnderecoDto(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,
        String cidade,
        String estado
) {
}
