package br.com.nsfatima.gestao.organizacao.domain.exception;

import java.util.UUID;

/**
 * Sinaliza violações de regra de negócio do contexto {@code grupoParoquial} (organização inexistente, hierarquia
 * inválida entre tipo e organização-mãe, exclusão de organização com dependências ativas etc.). Lançada por
 * {@code GrupoParoquialService} e tratada pelo handler de exceções da API para produzir uma resposta de erro
 * apropriada ao cliente.
 */
public class GrupoParoquialNegocioException extends RuntimeException {
    public GrupoParoquialNegocioException(String message) {
        super(message);
    }

    /**
     * Cria a exceção para o caso em que uma organização referenciada por ID não existe.
     *
     * @param id identificador da organização não encontrada
     * @return exceção pronta para ser lançada
     */
    public static GrupoParoquialNegocioException notFound(UUID id) {
        return new GrupoParoquialNegocioException("Organization not found with ID: " + id);
    }

    /**
     * Cria a exceção para o caso em que a exclusão de uma organização foi bloqueada por ela possuir
     * dependências ativas (membros vinculados ou eventos associados), regra que impede a perda de referências
     * órfãs no restante do sistema.
     *
     * @param id identificador da organização que não pode ser excluída
     * @return exceção pronta para ser lançada
     */
    public static GrupoParoquialNegocioException inUse(UUID id) {
        return new GrupoParoquialNegocioException(
                "Cannot delete organization " + id + " because it has associated members or events.");
    }
}
