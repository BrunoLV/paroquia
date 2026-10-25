package br.com.nsfatima.gestao.conselho.domain.exception;

/** O tipo de reunião escolhido não pode ser convocado pela organização informada. */
public class TipoReuniaoIncompativelException extends RuntimeException {

    public TipoReuniaoIncompativelException(String message) {
        super(message);
    }
}
