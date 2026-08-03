package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada ao tentar transferir a organização dona de um ativo patrimonial que está em um status
 * que impede a transferência ({@code EM_MANUTENCAO} ou {@code ALOCADO}), pois trocar o
 * proprietário nessas condições invalidaria a ocupação corrente.
 */
public class AtivoTransferenciaBloqueadaException extends RuntimeException {

    public AtivoTransferenciaBloqueadaException(String message) {
        super(message);
    }
}
