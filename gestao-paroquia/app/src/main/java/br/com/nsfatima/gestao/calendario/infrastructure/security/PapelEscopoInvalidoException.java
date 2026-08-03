package br.com.nsfatima.gestao.calendario.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;

public class PapelEscopoInvalidoException extends AccessDeniedException {

    public PapelEscopoInvalidoException(String message) {
        super(message);
    }
}
