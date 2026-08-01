package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import org.springframework.http.HttpStatus;

public sealed interface AtivoOperationResult {

    HttpStatus status();

    Object body();

    record Success(Object response, HttpStatus status) implements AtivoOperationResult {
        @Override
        public Object body() {
            return response;
        }
    }

    record Pending(Object response, HttpStatus status) implements AtivoOperationResult {
        @Override
        public Object body() {
            return response;
        }
    }
}
