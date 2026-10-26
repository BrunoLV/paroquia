package br.com.nsfatima.gestao.conselho.domain.exception;

import java.util.UUID;

/**
 * Lançada quando a ata referenciada não existe ou não é visível ao ator. Os dois casos são
 * indistinguíveis de propósito, para não revelar a existência de atas de outros órgãos.
 */
public class AtaNaoEncontradaException extends RuntimeException {

    public AtaNaoEncontradaException(UUID id) {
        super("Ata não encontrada com ID: " + id);
    }

    public AtaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
