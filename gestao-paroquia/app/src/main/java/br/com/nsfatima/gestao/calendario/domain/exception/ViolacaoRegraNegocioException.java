package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando uma operação no calendário viola uma regra de negócio do domínio eclesiástico
 * (ex.: ausência de justificativa obrigatória para submissões pelo Secretário).
 */
public class ViolacaoRegraNegocioException extends RuntimeException {

    public ViolacaoRegraNegocioException(String message) {
        super(message);
    }
}
