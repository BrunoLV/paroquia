package br.com.nsfatima.gestao.calendario.api.v1.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * Implementa a restrição {@link ValidEventoDates} usando reflexão: invoca os métodos de acesso
 * {@code inicio()} e {@code fim()} do objeto validado (tipicamente um record de DTO de evento) e
 * confirma que {@code fim} é estritamente posterior a {@code inicio}. Usar reflexão em vez de uma
 * interface comum permite aplicar a mesma anotação a DTOs distintos ({@code CreateEventoRequest},
 * {@code UpdateEventoRequest}) sem exigir uma hierarquia de tipos compartilhada entre eles.
 */
public class EventoDatesValidator implements ConstraintValidator<ValidEventoDates, Object> {

    /**
     * Verifica se {@code fim} é posterior a {@code inicio} no objeto validado. É permissiva por
     * design: retorna válido quando o valor é {@code null}, quando qualquer uma das datas está
     * ausente, ou quando a reflexão falha (ex: DTO sem os métodos {@code inicio()}/{@code fim()}) —
     * nesses casos, a obrigatoriedade das datas é responsabilidade de outras anotações no DTO, não
     * desta restrição.
     *
     * @param value objeto a ser validado, esperado expor {@code inicio()} e {@code fim()} retornando
     *     {@link Instant}
     * @param context contexto de validação padrão do Bean Validation, não utilizado nesta
     *     implementação
     * @return {@code true} se {@code fim} for posterior a {@code inicio}, ou se a validação não
     *     puder ser aplicada; {@code false} caso {@code fim} não seja posterior a {@code inicio}
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            Instant inicio = getInstant(value, "inicio");
            Instant fim = getInstant(value, "fim");

            if (inicio == null || fim == null) {
                return true;
            }

            return fim.isAfter(inicio);
        } catch (Exception e) {
            return true; // Or false, depending on how strict we want to be if methods are missing
        }
    }

    private Instant getInstant(Object value, String methodName) throws Exception {
        try {
            Method method = value.getClass().getMethod(methodName);
            Object result = method.invoke(value);
            if (result instanceof Instant) {
                return (Instant) result;
            }
        } catch (NoSuchMethodException e) {
            // Try as a field if method not found (though records have methods)
        }
        return null;
    }
}
