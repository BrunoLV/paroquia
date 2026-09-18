package br.com.nsfatima.gestao.fiel.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EnderecoDto(
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
     * @return a UF/estado
     */
    public String estado() {
        return uf;
    }
}
