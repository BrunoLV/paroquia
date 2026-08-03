package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando o endpoint manual de observação de evento recebe um tipo de
 * observação diferente de {@code NOTA} — o único tipo que pode ser criado
 * diretamente pelo usuário; os demais tipos são reservados a eventos gerados pelo
 * próprio sistema.
 */
public class ObservacaoTipoManualInvalidoException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo o tipo de observação inválido.
     *
     * @param message descrição do erro de tipo de observação manual inválido
     */
    public ObservacaoTipoManualInvalidoException(String message) {
        super(message);
    }
}
