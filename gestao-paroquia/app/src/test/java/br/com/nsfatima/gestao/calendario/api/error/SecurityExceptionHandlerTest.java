package br.com.nsfatima.gestao.calendario.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import br.com.nsfatima.gestao.iam.domain.exception.GerenciamentoUsuarioNegadoException;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

class SecurityExceptionHandlerTest {

    private final SecurityExceptionHandler handler =
            new SecurityExceptionHandler(mock(AuditoriaPersistenceService.class));
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void negativaDeGerenciamentoDeUsuarioResponde403ComMesmoCorpoDeAcessoNegado() {
        ResponseEntity<ValidationErrorResponse> domainDenied =
                handler.handleAccessDenied(new GerenciamentoUsuarioNegadoException("sem acesso"), request);
        ResponseEntity<ValidationErrorResponse> springDenied =
                handler.handleAccessDenied(new AccessDeniedException("sem acesso"), request);

        assertThat(domainDenied.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(domainDenied.getBody().errorCode()).isEqualTo(ErrorCodes.ACCESS_DENIED.name());
        assertThat(domainDenied.getBody().message()).isEqualTo(springDenied.getBody().message());
        assertThat(domainDenied.getBody().errors()).isEqualTo(springDenied.getBody().errors());
    }
}
