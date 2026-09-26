package br.com.nsfatima.gestao.organizacao.domain.model;

/**
 * Objeto de Valor (Value Object) imutável representando o endereço físico de uma Unidade Territorial.
 */
public record EnderecoUnidade(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {
    public EnderecoUnidade {
        if (uf != null) {
            String ufNormalizada = uf.trim().toUpperCase();
            if (ufNormalizada.length() > 2) {
                throw new IllegalArgumentException("UF não pode exceder 2 caracteres: " + uf);
            }
            uf = ufNormalizada;
        }
        if (cep != null) {
            cep = cep.trim();
        }
        if (logradouro != null) {
            logradouro = logradouro.trim();
        }
        if (numero != null) {
            numero = numero.trim();
        }
        if (complemento != null) {
            complemento = complemento.trim();
        }
        if (bairro != null) {
            bairro = bairro.trim();
        }
        if (cidade != null) {
            cidade = cidade.trim();
        }
    }

    public static EnderecoUnidade vazio() {
        return new EnderecoUnidade(null, null, null, null, null, null, null);
    }
}
