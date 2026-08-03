package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada quando um ativo patrimonial referenciado por id não é encontrado no inventário.
 */
public class AtivoPatrimonialNotFoundException extends RuntimeException {

    public AtivoPatrimonialNotFoundException(String message) {
        super(message);
    }
}
