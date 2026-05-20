package br.com.nsfatima.gestao.iam.domain.exception;

import java.util.UUID;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(UUID id) {
        super("Usuario nao encontrado: " + id);
    }
}
