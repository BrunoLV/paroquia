package br.com.nsfatima.gestao.calendario.api.v1.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.lang.reflect.Method;

public class EventDatesValidator implements ConstraintValidator<ValidEventDates, Object> {

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
