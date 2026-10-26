package br.com.nsfatima.gestao.conselho.domain.exception;

/** Transição de estado incompatível com a situação atual da ata (por exemplo, homologar um rascunho). */
public class TransicaoAtaInvalidaException extends RuntimeException {

    public TransicaoAtaInvalidaException(String message) {
        super(message);
    }
}
