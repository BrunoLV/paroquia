package br.com.nsfatima.gestao.iam.api.v1.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Política única de senha do IAM, aplicada a toda entrada de senha nova (criação administrativa,
 * troca pelo próprio usuário e redefinição administrativa), para que o mesmo valor não seja aceito
 * por um fluxo e recusado por outro. Exige no mínimo {@value SenhaValidaValidator#MINIMO_CARACTERES}
 * caracteres não brancos e no máximo {@value SenhaValidaValidator#MAXIMO_BYTES} bytes em UTF-8: o
 * BCrypt ignora silenciosamente tudo além do 72º byte, então senhas maiores dariam falsa sensação
 * de segurança. O limite é em bytes, não em caracteres, pois acentos ocupam mais de um byte.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SenhaValidaValidator.class)
public @interface SenhaValida {

    String message() default "A senha deve ter entre 8 caracteres e 72 bytes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
