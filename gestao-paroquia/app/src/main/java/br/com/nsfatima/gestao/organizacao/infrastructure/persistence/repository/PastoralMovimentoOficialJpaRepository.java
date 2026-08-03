package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.PastoralMovimentoOficialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório Spring Data para o catálogo oficial de Pastorais e Movimentos
 * ({@link PastoralMovimentoOficialEntity}).
 */
public interface PastoralMovimentoOficialJpaRepository extends JpaRepository<PastoralMovimentoOficialEntity, UUID> {
    /**
     * Verifica se já existe um item do catálogo com o nome informado, usado para impor a regra de unicidade
     * de nome.
     *
     * @param nome nome a verificar
     * @return {@code true} se já existir um item com este nome
     */
    boolean existsByNome(String nome);
}
