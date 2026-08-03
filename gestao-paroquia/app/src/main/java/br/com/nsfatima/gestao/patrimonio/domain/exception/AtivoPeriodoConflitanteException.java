package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada quando o período solicitado para uma alocação/reserva de ativo patrimonial se sobrepõe
 * a outra ocupação (alocação ou manutenção) já registrada para o mesmo ativo.
 */
public class AtivoPeriodoConflitanteException extends RuntimeException {

    public AtivoPeriodoConflitanteException(String message) {
        super(message);
    }
}
