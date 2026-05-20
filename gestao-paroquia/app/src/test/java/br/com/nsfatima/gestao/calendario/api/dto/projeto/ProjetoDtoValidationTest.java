package br.com.nsfatima.gestao.projeto.api.v1.dto;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
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

class ProjetoDtoValidationTest {

    private static Validator validator;
    private static final UUID ORG_ID = UUID.randomUUID();
    private static final Instant START = Instant.now();
    private static final Instant END = START.plusSeconds(3600);

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("ProjetoCreateRequest: should fail when name is too long")
    void projetoCreateRequest_NameTooLong() {
        String longName = "A".repeat(161);
        ProjetoCreateRequest request = new ProjetoCreateRequest(longName, "Descricao", ORG_ID, START, END);

        Set<ConstraintViolation<ProjetoCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long name");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }

    @Test
    @DisplayName("ProjetoCreateRequest: should fail when description is too long")
    void projetoCreateRequest_DescriptionTooLong() {
        String longDescription = "A".repeat(2001);
        ProjetoCreateRequest request = new ProjetoCreateRequest("Nome", longDescription, ORG_ID, START, END);

        Set<ConstraintViolation<ProjetoCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long description");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricao")));
    }

    @Test
    @DisplayName("ProjetoPatchRequest: should fail when name is too long")
    void projetoPatchRequest_NameTooLong() {
        String longName = "A".repeat(161);
        ProjetoPatchRequest request = new ProjetoPatchRequest(longName, "Descricao", ORG_ID, START, END, ProjetoStatus.ATIVO);

        Set<ConstraintViolation<ProjetoPatchRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long name");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }

    @Test
    @DisplayName("ProjetoPatchRequest: should fail when name is blank")
    void projetoPatchRequest_NameBlank() {
        ProjetoPatchRequest request = new ProjetoPatchRequest(" ", "Descricao", ORG_ID, START, END, ProjetoStatus.ATIVO);

        Set<ConstraintViolation<ProjetoPatchRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for blank name");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }
}
