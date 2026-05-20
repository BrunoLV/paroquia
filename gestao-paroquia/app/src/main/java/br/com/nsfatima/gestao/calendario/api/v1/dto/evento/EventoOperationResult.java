package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import org.springframework.http.HttpStatus;

/**
 * Sealed interface for event operation results, ensuring explicit types for success and pending outcomes.
 */
public sealed interface EventoOperationResult {
    
    HttpStatus status();
    Object body();

    record Success(Object response, HttpStatus status) implements EventoOperationResult {
        @Override public Object body() { return response; }
    }

    record Pending(Object response, HttpStatus status) implements EventoOperationResult {
        @Override public Object body() { return response; }
    }
}
