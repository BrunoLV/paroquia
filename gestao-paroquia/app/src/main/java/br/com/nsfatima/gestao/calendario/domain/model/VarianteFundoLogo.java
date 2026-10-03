package br.com.nsfatima.gestao.calendario.domain.model;

/**
 * Classifica a variante de contraste do logotipo oficial da paróquia:
 * {@code CLARO} para exibição sobre fundos claros (ex.: páginas em tema light, relatórios PDF brancos) e
 * {@code ESCURO} para exibição sobre fundos escuros (ex.: páginas em tema dark, capas de apresentação com cor primária).
 */
public enum VarianteFundoLogo {
    CLARO,
    ESCURO;

    /**
     * Resolve a variante a partir de texto (case-insensitive).
     *
     * @param valor texto identificador (ex.: "claro", "escuro")
     * @return a variante correspondente
     * @throws IllegalArgumentException se o valor não corresponder a nenhuma variante válida
     */
    public static VarianteFundoLogo fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Variante do logotipo não pode ser nula ou vazia.");
        }
        String normalizado = valor.trim().replaceFirst("(?i)\\.(png|jpe?g|webp|ico)$", "").toUpperCase();
        for (VarianteFundoLogo variante : values()) {
            if (variante.name().equals(normalizado)) {
                return variante;
            }
        }
        throw new IllegalArgumentException(
                "Variante de logotipo inválida: '" + valor + "'. Valores permitidos: CLARO, ESCURO.");
    }
}
