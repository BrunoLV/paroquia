package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.PapelGlobalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso a persistência das atribuições de papel global ({@link PapelGlobalEntity}).
 */
@Repository
public interface PapelGlobalJpaRepository extends JpaRepository<PapelGlobalEntity, UUID> {

    List<PapelGlobalEntity> findByFielIdAndAtivoTrue(UUID fielId);

    Optional<PapelGlobalEntity> findByFielIdAndPapel(UUID fielId, String papel);
}
