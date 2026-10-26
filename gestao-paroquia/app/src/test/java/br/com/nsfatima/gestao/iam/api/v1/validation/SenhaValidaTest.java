package br.com.nsfatima.gestao.iam.api.v1.validation;

import br.com.nsfatima.gestao.iam.api.v1.dto.AlterarMinhaSenhaRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.CriarUsuarioRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.RedefinirSenhaRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SenhaValidaTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve rejeitar senha curta nos três fluxos de definição de senha")
    void deveRejeitarSenhaCurtaEmTodosOsFluxos() {
        assertThat(validator.validate(new RedefinirSenhaRequest("1234567"))).isNotEmpty();
        assertThat(validator.validate(new AlterarMinhaSenhaRequest("atual", "1234567"))).isNotEmpty();
        assertThat(validator.validate(new CriarUsuarioRequest("maria.silva", "1234567", UUID.randomUUID())))
                .isNotEmpty();
    }

    @Test
    @DisplayName("Deve aceitar senha de 8 caracteres nos três fluxos")
    void deveAceitarSenhaMinima() {
        assertThat(validator.validate(new RedefinirSenhaRequest("12345678"))).isEmpty();
        assertThat(validator.validate(new AlterarMinhaSenhaRequest("atual", "12345678"))).isEmpty();
        assertThat(validator.validate(new CriarUsuarioRequest("maria.silva", "12345678", UUID.randomUUID()))).isEmpty();
    }

    @Test
    @DisplayName("Deve limitar a senha a 72 bytes UTF-8 (limite do BCrypt), não a 72 caracteres")
    void deveLimitarEm72Bytes() {
        String noLimite = "a".repeat(72);
        String acimaDoLimite = "a".repeat(73);
        String multibyte = "ç".repeat(37);

        assertThat(validator.validate(new RedefinirSenhaRequest(noLimite))).isEmpty();
        assertThat(validator.validate(new RedefinirSenhaRequest(acimaDoLimite))).isNotEmpty();
        assertThat(validator.validate(new RedefinirSenhaRequest(multibyte))).isNotEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar senha em branco")
    void deveRejeitarSenhaEmBranco() {
        assertThat(validator.validate(new RedefinirSenhaRequest("         "))).isNotEmpty();
        assertThat(validator.validate(new RedefinirSenhaRequest(null))).isNotEmpty();
    }
}
