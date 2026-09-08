package br.com.nsfatima.gestao.local.domain.model;

/**
 * Objeto de Valor (Value Object) imutável que representa o endereço físico estruturado
 * de um Local paroquial (sala, salão, quadra, auditório).
 *
 * <p>Invariantes de domínio: a UF, quando informada, não pode exceder 2 caracteres
 * e é normalizada para letras maiúsculas.
 */
public record EnderecoLocal(
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
    public EnderecoLocal {
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
     * Cria um {@code EnderecoLocal} vazio, útil quando o local não possui dados de localização detalhados.
     *
     * @return instância de endereço vazio
     */
    public static EnderecoLocal vazio() {
        return new EnderecoLocal(null, null, null, null, null, null, null);
    }
}
