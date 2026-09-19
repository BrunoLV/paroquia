package br.com.nsfatima.gestao.calendario.api.dto.aprovacao;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.CriarAprovacaoRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecidirAprovacaoRequest;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AprovacaoDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("CriarAprovacaoRequest: should fail when eventoId is null")
    void aprovacaoCreateRequest_EventoIdNull() {
        CriarAprovacaoRequest request = new CriarAprovacaoRequest(null, TipoSolicitacaoInput.CRIACAO_EVENTO);

        Set<ConstraintViolation<CriarAprovacaoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("eventoId")));
    }

    @Test
    @DisplayName("DecidirAprovacaoRequest: should fail when status is null")
    void aprovacaoDecisionRequest_StatusNull() {
        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(null, "Obs");

        Set<ConstraintViolation<DecidirAprovacaoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }

    @Test
    @DisplayName("DecidirAprovacaoRequest: should fail when observacao is too long")
    void aprovacaoDecisionRequest_ObservacaoTooLong() {
        String longObs = "A".repeat(2001);
        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, longObs);

        Set<ConstraintViolation<DecidirAprovacaoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("observacao")));
    }
}
