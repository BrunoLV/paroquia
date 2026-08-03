package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoNotaRevisaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório de persistência de {@link ObservacaoNotaRevisaoEntity}, responsável pelo histórico
 * de notas de revisão registradas sobre uma observação de evento (ex.: correções ou
 * complementos feitos após a publicação inicial da observação).
 */
public interface ObservacaoNotaRevisaoJpaRepository extends JpaRepository<ObservacaoNotaRevisaoEntity, UUID> {

    /**
     * Busca o histórico de notas de revisão de uma observação, ordenado cronologicamente pela
     * data de revisão, para reconstituir a linha do tempo de alterações feitas sobre ela.
     *
     * @param observacaoId identificador da observação revisada
     * @return notas de revisão associadas à observação informada, ordenadas por data de revisão e id ascendentes
     */
    List<ObservacaoNotaRevisaoEntity> findByObservacaoIdOrderByRevisadoEmUtcAscIdAsc(UUID observacaoId);
}
