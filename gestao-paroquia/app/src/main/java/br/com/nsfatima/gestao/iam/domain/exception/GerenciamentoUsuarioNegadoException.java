package br.com.nsfatima.gestao.iam.domain.exception;

/**
 * Sinaliza que o ator não tem autoridade para gerenciar o usuário-alvo ou a organização informada
 * (coordenação inexistente ou sem organização em comum). É a exceção semântica lançada pela política
 * de autorização de gerenciamento de usuários, mantendo o domínio livre do Spring Security; a camada
 * {@code api} a traduz para HTTP 403 com o mesmo corpo de um acesso negado.
 */
public class GerenciamentoUsuarioNegadoException extends RuntimeException {

    /**
     * Constrói a exceção com o motivo da negativa.
     *
     * @param mensagem motivo da negativa, sem dados pessoais
     */
    public GerenciamentoUsuarioNegadoException(String mensagem) {
        super(mensagem);
    }
}
