package br.com.nsfatima.gestao.sacramento.domain.exception;

import java.util.List;

/** Violação de requisito canônico do teor, com todas as pendências detectadas na homologação. */
public class AssentoInvalidoException extends RuntimeException {

    private final List<String> pendencias;

    public AssentoInvalidoException(List<String> pendencias) {
        super("Assento não atende aos requisitos canônicos: " + String.join("; ", pendencias));
        this.pendencias = List.copyOf(pendencias);
    }

    public List<String> getPendencias() {
        return pendencias;
    }
}
