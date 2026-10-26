package br.com.nsfatima.gestao.reserva.api.v1.controller;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoEncontradaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaTransicaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaExceptionHandlerTest {

    @Mock private AuditoriaPersistenceService auditLogPersistenceService;
    @Mock private HttpServletRequest request;

    private ReservaExceptionHandler handler() {
        return new ReservaExceptionHandler(auditLogPersistenceService);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("handleConflito retorna 409 com lista de conflitos mapeados")
    void deveTratarConflito() {
        ConflitoDetalhe detalhe = new ConflitoDetalhe(
                UUID.randomUUID(), "Salão", TipoItemReserva.LOCAL,
                Instant.now(), Instant.now().plusSeconds(3600), UUID.randomUUID(), "Responsável", "desc");
        ReservaConflitoException ex = new ReservaConflitoException("Conflito de agenda", List.of(detalhe));

        ResponseEntity<Map<String, Object>> response = handler().handleConflito(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "Conflito de agenda");
        assertThat((List<?>) response.getBody().get("conflitos")).hasSize(1);
    }

    @Test
    @DisplayName("handleNaoAutorizada retorna 403 e registra auditoria de negação de segurança")
    void deveTratarNaoAutorizada() {
        when(request.getRequestURI()).thenReturn("/api/v1/reservas/123/aprovar");
        ReservaNaoAutorizadaException ex = new ReservaNaoAutorizadaException("Sem permissão para aprovar.");

        ResponseEntity<ValidationErrorResponse> response = handler().handleNaoAutorizada(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("Sem permissão para aprovar.");
        verify(auditLogPersistenceService).log(any(), eq("security-denied"), eq("/api/v1/reservas/123/aprovar"),
                eq("DENY"), any());
    }

    @Test
    @DisplayName("handleNotFound retorna 404 com mensagem da exceção")
    void deveTratarNotFound() {
        UUID id = UUID.randomUUID();
        ReservaNaoEncontradaException ex = new ReservaNaoEncontradaException(id);

        ResponseEntity<ValidationErrorResponse> response = handler().handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().errors()).hasSize(1);
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("reservaId");
    }

    @Test
    @DisplayName("handleOperacaoInvalida retorna 400 com código de violação de regra de domínio")
    void deveTratarOperacaoInvalida() {
        ReservaOperacaoInvalidaException ex = new ReservaOperacaoInvalidaException("Ativo em manutenção.");

        ResponseEntity<ValidationErrorResponse> response = handler().handleOperacaoInvalida(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Ativo em manutenção.");
    }

    @Test
    @DisplayName("handleTransicaoInvalida retorna 400 em vez de falha interna para transição de estado ilegal")
    void deveTratarTransicaoInvalida() {
        ReservaTransicaoInvalidaException ex = new ReservaTransicaoInvalidaException("Reserva retirada.");

        ResponseEntity<ValidationErrorResponse> response = handler().handleTransicaoInvalida(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Reserva retirada.");
    }
}
