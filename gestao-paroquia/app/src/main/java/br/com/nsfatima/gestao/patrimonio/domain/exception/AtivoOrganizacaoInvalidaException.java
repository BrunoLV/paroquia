package br.com.nsfatima.gestao.patrimonio.domain.exception;

/**
 * Lançada quando a organização informada em uma operação de patrimônio não é elegível como
 * proprietária de ativos (ver {@code AtivoOrganizacaoPolicy}) — ex.: transferência ou cadastro
 * apontando para um tipo de organização que não pode possuir bens patrimoniais.
 */
public class AtivoOrganizacaoInvalidaException extends RuntimeException {

    public AtivoOrganizacaoInvalidaException(String message) {
        super(message);
    }
}
