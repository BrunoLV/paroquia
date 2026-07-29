package br.com.nsfatima.gestao.conselho.domain.exception;

import java.util.UUID;

public class CpcDecisaoNotFoundException extends RuntimeException {

    public CpcDecisaoNotFoundException(UUID id) {
        super("Decisão/Ata de conselho não encontrada com ID: " + id);
    }
}
