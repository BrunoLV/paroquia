package br.com.nsfatima.gestao.reserva.domain.exception;

/**
 * Transição de estado ilegal do ciclo de vida da reserva (por exemplo, cancelar um bem já retirado ou finalizar
 * o uso de um local ainda não confirmado). Especializa {@link IllegalStateException} para que a API a traduza em
 * erro de regra de negócio em vez de falha interna.
 */
public class ReservaTransicaoInvalidaException extends IllegalStateException {

    public ReservaTransicaoInvalidaException(String message) {
        super(message);
    }
}
