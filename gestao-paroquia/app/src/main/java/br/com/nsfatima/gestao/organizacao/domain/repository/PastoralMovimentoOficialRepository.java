package br.com.nsfatima.gestao.organizacao.domain.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência (Ports &amp; Adapters) para o catálogo oficial de Pastorais e Movimentos
 * ({@link PastoralMovimentoOficial}), isolando o domínio dos detalhes de JPA.
 */
public interface PastoralMovimentoOficialRepository {
    /**
     * Persiste (insere ou atualiza) o item do catálogo oficial informado.
     *
     * @param pastoralOficial item a ser salvo
     */
    void save(PastoralMovimentoOficial pastoralOficial);

    /**
     * Busca um item do catálogo oficial pelo seu identificador.
     *
     * @param id identificador do item
     * @return o item encontrado, ou {@link Optional#empty()} se não existir
     */
    Optional<PastoralMovimentoOficial> findById(UUID id);

    /**
     * Lista todos os itens do catálogo oficial de Pastorais e Movimentos.
     *
     * @return lista de todos os itens cadastrados
     */
    List<PastoralMovimentoOficial> findAll();

    /**
     * Remove definitivamente o item do catálogo oficial identificado.
     *
     * @param id identificador do item a ser removido
     */
    void delete(UUID id);

    /**
     * Verifica se já existe um item do catálogo com o nome informado, usado para impor a regra de unicidade
     * de nome ao criar ou renomear um item.
     *
     * @param nome nome a verificar
     * @return {@code true} se já existir um item com este nome
     */
    boolean existsByNome(String nome);
}
