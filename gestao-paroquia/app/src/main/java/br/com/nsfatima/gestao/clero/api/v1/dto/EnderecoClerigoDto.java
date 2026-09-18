package br.com.nsfatima.gestao.clero.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * DTO para trânsito de dados de endereço estruturado do Clérigo na API REST.
 */
public record EnderecoClerigoDto(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        @JsonAlias("estado") String uf
) {
    /**
     * Alias de conveniência para retrocompatibilidade.
     *
     * @return a sigla do estado (UF)
     */
    public String estado() {
        return uf;
    }
}
