package br.com.nsfatima.gestao.projeto.domain.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um {@code Projeto} referenciado por ID não é encontrado, seja em
 * operações de leitura, seja nas transições do ciclo de vida (iniciar, concluir, cancelar).
 */
public class ProjetoNaoEncontradoException extends RuntimeException {

    /**
     * Constrói a exceção com uma mensagem descritiva contendo o ID do projeto não localizado.
     *
     * @param id identificador do projeto que não foi encontrado.
     */
    public ProjetoNaoEncontradoException(UUID id) {
        super("Projeto não encontrado com ID: " + id);
    }
}
