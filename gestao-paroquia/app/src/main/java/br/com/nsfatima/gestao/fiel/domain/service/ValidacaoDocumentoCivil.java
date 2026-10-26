package br.com.nsfatima.gestao.fiel.domain.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Regras de validação do documento civil de um {@code Fiel}: país de origem em ISO-3166 alpha-3,
 * CPF obrigatório e válido (dígito verificador) para brasileiros, RNE/RNM ou passaporte para
 * estrangeiros.
 *
 * <p>Antes vivia como {@code static} em {@code CriarFielUseCase}, invocado por chamada cruzada de
 * {@code AtualizarFielUseCase} (anti-padrão). Isolado aqui, é injetado por ambos e testável sozinho.</p>
 */
public class ValidacaoDocumentoCivil {

    private static final Set<String> PAISES_ISO3 = Arrays.stream(Locale.getISOCountries())
            .map(c -> new Locale.Builder().setRegion(c).build().getISO3Country())
            .collect(Collectors.toUnmodifiableSet());

    public void validarPais(String pais) {
        if (pais == null || !PAISES_ISO3.contains(pais.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("País de origem deve usar ISO-3166 alpha-3.");
        }
    }

    public void validarDocumento(String pais, String cpf, String tipoDocumentoEstrangeiro, String numeroDocumento) {
        boolean brasil = "BRA".equalsIgnoreCase(pais);
        String limpo = cpf != null ? cpf.replaceAll("\\D", "") : null;
        boolean cpfPresente = limpo != null && limpo.length() == 11;
        if (brasil && (!cpfPresente || (!cpfValido(limpo) && !isCpfDevDummy(limpo)))) {
            throw new IllegalArgumentException("CPF brasileiro é obrigatório e deve ser válido.");
        }
        if (!brasil && (tipoDocumentoEstrangeiro == null || numeroDocumento == null || numeroDocumento.isBlank())) {
            throw new IllegalArgumentException("RNE/RNM ou passaporte é obrigatório para estrangeiros.");
        }
        if (!brasil && !"RNE_RNM".equals(tipoDocumentoEstrangeiro) && !"PASSAPORTE".equals(tipoDocumentoEstrangeiro)) {
            throw new IllegalArgumentException("Documento estrangeiro deve ser RNE_RNM ou PASSAPORTE.");
        }
    }

    /**
     * Verifica o CPF (11 dígitos e dígitos verificadores) de uma pessoa que não é cadastrada como fiel, como
     * o responsável legal de um menor, sem a tolerância a CPF fictício de desenvolvimento.
     *
     * @param cpf CPF com ou sem máscara
     * @return {@code true} se o CPF é bem formado e tem dígitos verificadores corretos
     */
    public boolean cpfEstritamenteValido(String cpf) {
        String limpo = cpf == null ? "" : cpf.replaceAll("\\D", "");
        return limpo.length() == 11 && cpfValido(limpo);
    }

    private static boolean isCpfDevDummy(String cpf) {
        return cpf != null && cpf.startsWith("000000000");
    }

    private static boolean cpfValido(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digito(cpf, 9) == cpf.charAt(9) - '0' && digito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digito(String cpf, int limite) {
        int soma = 0;
        for (int i = 0; i < limite; i++) {
            soma += (cpf.charAt(i) - '0') * (limite + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
