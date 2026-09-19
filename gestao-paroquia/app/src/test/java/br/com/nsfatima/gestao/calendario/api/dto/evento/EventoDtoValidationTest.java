package br.com.nsfatima.gestao.calendario.api.dto.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.AtualizarEventoRequest;
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
    @DisplayName("CriarEventoRequest: should fail when title is too long")
    void createEventoRequest_TitleTooLong() {
        String longTitle = "A".repeat(161);
        CriarEventoRequest request = new CriarEventoRequest(
                longTitle,
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                null,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CriarEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("CriarEventoRequest: should fail when description is too long")
    void createEventoRequest_DescriptionTooLong() {
        String longDescription = "A".repeat(4001);
        CriarEventoRequest request = new CriarEventoRequest(
                "Titulo",
                longDescription,
                null,
                UUID.randomUUID(),
                null,
                null,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CriarEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long description");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricao")));
    }

    @Test
    @DisplayName("AtualizarEventoRequest: should fail when title is blank")
    void updateEventoRequest_TitleBlank() {
        AtualizarEventoRequest request = new AtualizarEventoRequest(
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
                null,
                null
        );

        Set<ConstraintViolation<AtualizarEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for blank title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("AtualizarEventoRequest: should fail when title is too long")
    void updateEventoRequest_TitleTooLong() {
        String longTitle = "A".repeat(161);
        AtualizarEventoRequest request = new AtualizarEventoRequest(
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
                null,
                null
        );

        Set<ConstraintViolation<AtualizarEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("CriarEventoRequest: should fail when end date is before start date")
    void createEventoRequest_EndBeforeStart() {
        Instant now = Instant.now();
        CriarEventoRequest request = new CriarEventoRequest(
                "Titulo",
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                null,
                now.plusSeconds(3600),
                now,
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CriarEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for invalid dates");
    }

    @Test
    @DisplayName("CriarEventoRequest e AtualizarEventoRequest: devem suportar campo liberarConflitoAgenda")
    void dto_LiberarConflitoAgendaField() {
        CriarEventoRequest createReq = new CriarEventoRequest(
                "Titulo", "Desc", null, UUID.randomUUID(), null, null, null,
                Instant.now(), Instant.now().plusSeconds(3600), EventoStatusInput.RASCUNHO,
                null, null, true
        );
        assertTrue(createReq.isLiberarConflitoAgenda());

        AtualizarEventoRequest updateReq = new AtualizarEventoRequest(
                "Titulo", "Desc", null, Instant.now(), Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO, null, null, UUID.randomUUID(), null, null, null, null, null, true
        );
        assertTrue(updateReq.isLiberarConflitoAgenda());
    }
}
