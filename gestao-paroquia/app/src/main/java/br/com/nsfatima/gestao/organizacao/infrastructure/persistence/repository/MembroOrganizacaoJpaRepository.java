package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembroOrganizacaoJpaRepository extends JpaRepository<MembroOrganizacaoEntity, UUID> {
    List<MembroOrganizacaoEntity> findByUsuarioIdAndAtivoTrue(UUID usuarioId);
    List<MembroOrganizacaoEntity> findByUsuarioId(UUID usuarioId);

    Optional<MembroOrganizacaoEntity> findByOrganizacaoIdAndPapelAndAtivoTrue(UUID organizacaoId, String papel);

    Optional<MembroOrganizacaoEntity> findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(UUID organizacaoId, String papel, UUID igrejaVinculoId);

    Optional<MembroOrganizacaoEntity> findByOrganizacaoIdAndPapelAndIgrejaVinculoIdNullAndAtivoTrue(UUID organizacaoId, String papel);

    Optional<MembroOrganizacaoEntity> findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(UUID usuarioId, UUID organizacaoId, String papel, UUID igrejaVinculoId);

    List<MembroOrganizacaoEntity> findByOrganizacaoIdAndAtivoTrue(UUID organizacaoId);

    List<MembroOrganizacaoEntity> findByIgrejaVinculoIdAndAtivoTrue(UUID igrejaVinculoId);

    boolean existsByOrganizacaoId(UUID organizacaoId);
}
