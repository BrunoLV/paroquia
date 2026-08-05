package br.com.nsfatima.gestao.fiel.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * Estado de vida / vocação de um {@link Fiel}, distinguindo clero (que também pode ter um {@code Usuario}
 * com papel global no IAM, ver {@code PapelGlobal}) de leigos e religiosos. É um atributo da pessoa, não um
 * papel de sistema — um {@code Fiel} continua {@code SACERDOTE} independente de ter ou não conta de login.
 */
public enum Vocacao {
    LEIGO,
    SACERDOTE,
    DIACONO,
    RELIGIOSO;

    /**
     * Converte um valor persistido/recebido de fora para a constante correspondente, normalizando
     * maiúsculas/minúsculas, espaços nas extremidades e separador ({@code _} vs {@code -}).
     *
     * @param rawValue valor bruto vindo do banco de dados ou de uma requisição externa
     * @return a {@link Vocacao} correspondente, ou {@code null} se {@code rawValue} for nulo, em branco
     *     ou não corresponder a nenhuma vocação conhecida
     */
    public static Vocacao fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");

        for (Vocacao vocacao : values()) {
            if (vocacao.storedValue().equals(normalized) || vocacao.name().equalsIgnoreCase(rawValue.trim())) {
                return vocacao;
            }
        }
        return null;
    }

    /**
     * Converte um valor recebido via JSON para a constante correspondente, delegando para
     * {@link #fromStoredValue(String)}.
     *
     * @param rawValue valor bruto recebido na requisição
     * @return a {@link Vocacao} correspondente, ou {@code null} se não reconhecida
     */
    @JsonCreator
    public static Vocacao fromJson(String rawValue) {
        return fromStoredValue(rawValue);
    }

    /**
     * Retorna o código estável usado para persistir esta vocação (independente do nome do enum em código).
     *
     * @return o código de armazenamento desta vocação
     */
    public String storedValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
