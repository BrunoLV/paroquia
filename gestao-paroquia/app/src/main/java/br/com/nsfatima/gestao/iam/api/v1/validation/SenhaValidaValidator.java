package br.com.nsfatima.gestao.iam.api.v1.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

/**
 * Implementa {@link SenhaValida}. Valores {@code null} são inválidos: a obrigatoriedade da senha
 * faz parte da própria política, e a anotação substitui {@code @NotBlank}/{@code @Size} nos DTOs.
 */
public class SenhaValidaValidator implements ConstraintValidator<SenhaValida, String> {

    static final int MINIMO_CARACTERES = 8;
    static final int MAXIMO_BYTES = 72;

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        return atende(senha);
    }

    /** Aplica a política fora do Bean Validation (ex.: campos opcionais que só validam se presentes). */
    public static boolean atende(String senha) {
        if (senha == null || senha.isBlank()) {
            return false;
        }
        return senha.length() >= MINIMO_CARACTERES
                && senha.getBytes(StandardCharsets.UTF_8).length <= MAXIMO_BYTES;
    }
}
