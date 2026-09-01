package br.com.nsfatima.gestao.fiel.domain.model;

/**
 * Objeto de Valor (Value Object) imutável representando o Endereço residencial/de contato do Fiel.
 */
public record Endereco(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cep,
        String cidade,
        String estado
) {
}
