package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("CreateEventoRequest: should fail when title is too long")
    void createEventoRequest_TitleTooLong() {
        String longTitle = "A".repeat(161);
        CreateEventoRequest request = new CreateEventoRequest(
                longTitle,
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CreateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("CreateEventoRequest: should fail when description is too long")
    void createEventoRequest_DescriptionTooLong() {
        String longDescription = "A".repeat(4001);
        CreateEventoRequest request = new CreateEventoRequest(
                "Titulo",
                longDescription,
                null,
                UUID.randomUUID(),
                null,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CreateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long description");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricao")));
    }

    @Test
    @DisplayName("UpdateEventoRequest: should fail when title is blank")
    void updateEventoRequest_TitleBlank() {
        UpdateEventoRequest request = new UpdateEventoRequest(
                " ",
                "Descricao",
                null,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null,
                UUID.randomUUID(),
                null,
                null,
                null
        );

        Set<ConstraintViolation<UpdateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for blank title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("UpdateEventoRequest: should fail when title is too long")
    void updateEventoRequest_TitleTooLong() {
        String longTitle = "A".repeat(161);
        UpdateEventoRequest request = new UpdateEventoRequest(
                longTitle,
                "Descricao",
                null,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null,
                UUID.randomUUID(),
                null,
                null,
                null
        );

        Set<ConstraintViolation<UpdateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("CreateEventoRequest: should fail when end date is before start date")
    void createEventoRequest_EndBeforeStart() {
        Instant now = Instant.now();
        CreateEventoRequest request = new CreateEventoRequest(
                "Titulo",
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                now.plusSeconds(3600),
                now,
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CreateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for invalid dates");
    }
}
