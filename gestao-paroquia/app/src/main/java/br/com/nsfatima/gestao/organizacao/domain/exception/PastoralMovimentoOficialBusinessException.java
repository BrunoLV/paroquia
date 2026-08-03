package br.com.nsfatima.gestao.organizacao.domain.exception;

import java.util.UUID;

/**
 * Sinaliza violações de regra de negócio ao gerenciar o catálogo oficial de Pastorais e Movimentos (item
 * inexistente, nome obrigatório ausente, nome duplicado, tipo diferente de {@code PASTORAL}/{@code MOVIMENTO}).
 * Lançada por {@code PastoralMovimentoOficialService} e pelo controller ao consultar um item inexistente.
 */
public class PastoralMovimentoOficialBusinessException extends RuntimeException {
    public PastoralMovimentoOficialBusinessException(String message) {
        super(message);
    }

    /**
     * Cria a exceção para o caso em que um item do catálogo oficial referenciado por ID não existe.
     *
     * @param id identificador do item não encontrado
     * @return exceção pronta para ser lançada
     */
    public static PastoralMovimentoOficialBusinessException notFound(UUID id) {
        return new PastoralMovimentoOficialBusinessException("Pastoral or Movimento Oficial not found with ID: " + id);
    }
}
