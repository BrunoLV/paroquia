package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

/**
 * Acesso a persistência de {@link UnidadeTerritorialEntity}.
 */
@Repository
public interface UnidadeTerritorialJpaRepository extends JpaRepository<UnidadeTerritorialEntity, UUID> {

    Optional<UnidadeTerritorialEntity> findByNomeIgnoreCase(String nome);

    List<UnidadeTerritorialEntity> findByTipoAndAtivoTrue(TipoUnidadeTerritorial tipo);
}
