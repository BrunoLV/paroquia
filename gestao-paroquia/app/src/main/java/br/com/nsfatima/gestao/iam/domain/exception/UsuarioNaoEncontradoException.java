package br.com.nsfatima.gestao.iam.domain.exception;

import java.util.UUID;

/**
 * Sinaliza que nenhum usuário existe com o id informado. Lançada pelos casos de uso e serviços de IAM
 * (consulta, atualização, aprovação, troca/reset de senha, atualização de papéis) sempre que o
 * {@code UsuarioJpaRepository} não encontra a entidade — tratada centralmente para responder 404 na API.
 */
public class UsuarioNaoEncontradoException extends RuntimeException {

    /**
     * Constrói a exceção com o id do usuário ausente embutido na mensagem.
     *
     * @param id identificador do usuário que não foi encontrado, incluído na mensagem de erro
     */
    public UsuarioNaoEncontradoException(UUID id) {
        super("Usuário não encontrado com ID: " + id);
    }
}
