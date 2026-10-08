package br.com.nsfatima.gestao.organizacao.domain.exception;

import java.util.UUID;

/**
 * Sinaliza violações de regra de negócio do contexto {@code organizacao} (organização inexistente, hierarquia
 * inválida entre tipo e organização-mãe, exclusão de organização com dependências ativas etc.). Lançada por
 * {@code OrganizacaoService} e tratada pelo handler de exceções da API para produzir uma resposta de erro
 * apropriada ao cliente.
 */
public class OrganizacaoNegocioException extends RuntimeException {
    public OrganizacaoNegocioException(String message) {
        super(message);
    }

    /**
     * Cria a exceção para o caso em que uma organização referenciada por ID não existe.
     *
     * @param id identificador da organização não encontrada
     * @return exceção pronta para ser lançada
     */
    public static OrganizacaoNegocioException notFound(UUID id) {
        return new OrganizacaoNegocioException("Organização não encontrada com o ID: " + id);
    }

    /**
     * Cria a exceção para o caso em que a exclusão de uma organização foi bloqueada por ela possuir
     * dependências ativas (membros vinculados ou eventos associados), regra que impede a perda de referências
     * órfãs no restante do sistema.
     *
     * @param id identificador da organização que não pode ser excluída
     * @return exceção pronta para ser lançada
     */
    public static OrganizacaoNegocioException inUse(UUID id) {
        return new OrganizacaoNegocioException(
                "Não é possível excluir a organização " + id + " porque ela possui membros ou eventos associados.");
    }
}
