package br.com.nsfatima.gestao.calendario.api.v1.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restrição de validação em nível de classe que exige que a data de término ({@code fim}) de um
 * evento seja estritamente posterior à data de início ({@code inicio}), impedindo o cadastro de
 * eventos com duração zero ou negativa. Aplicada a DTOs de requisição de evento (ex:
 * {@code CriarEventoRequest}) que expõem os métodos de acesso {@code inicio()} e {@code fim()}
 * retornando {@link java.time.Instant}. A validação é silenciosamente permissiva (retorna válido)
 * quando qualquer uma das datas está ausente ou quando o alvo não expõe os métodos esperados — a
 * obrigatoriedade das datas em si é responsabilidade de outras anotações de validação no DTO.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidadorDatasEvento.class)
public @interface ValidaDatasEvento {
    String message() default "Data de término do evento deve ser posterior à data de início";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
