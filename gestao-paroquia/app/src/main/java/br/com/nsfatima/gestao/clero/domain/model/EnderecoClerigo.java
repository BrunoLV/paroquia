package br.com.nsfatima.gestao.clero.domain.model;

/**
 * Objeto de Valor (Value Object) imutável representando o Endereço residencial do Clérigo.
 *
 * <p>Invariantes de domínio: a UF, quando informada, não pode exceder 2 caracteres
 * e é normalizada para letras maiúsculas.</p>
 */
public record EnderecoClerigo(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf
) {
    /**
     * Construtor compacto para normalização e validação de invariantes.
     */
    public EnderecoClerigo {
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

    /**
     * Cria um {@code EnderecoClerigo} vazio.
     *
     * @return instância de endereço vazio
     */
    public static EnderecoClerigo vazio() {
        return new EnderecoClerigo(null, null, null, null, null, null, null);
    }

    /**
     * Formata o endereço estruturado em representação textual unificada.
     *
     * @return endereço formatado ou string vazia se não informado
     */
    public String formatado() {
        if (logradouro == null || logradouro.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(logradouro);
        if (numero != null && !numero.isBlank()) {
            sb.append(", ").append(numero);
        }
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" (").append(complemento).append(")");
        }
        if (bairro != null && !bairro.isBlank()) {
            sb.append(" - ").append(bairro);
        }
        if (cidade != null && !cidade.isBlank()) {
            sb.append(", ").append(cidade);
        }
        if (uf != null && !uf.isBlank()) {
            sb.append("/").append(uf);
        }
        if (cep != null && !cep.isBlank()) {
            sb.append(" - CEP: ").append(cep);
        }
        return sb.toString();
    }
}
