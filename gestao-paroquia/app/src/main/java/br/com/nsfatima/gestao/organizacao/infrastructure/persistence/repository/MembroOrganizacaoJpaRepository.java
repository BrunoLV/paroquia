package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MembroOrganizacaoJpaRepository extends JpaRepository<MembroOrganizacaoEntity, UUID> {
    List<MembroOrganizacaoEntity> findByUsuarioIdAndAtivoTrue(UUID usuarioId);

    boolean existsByOrganizacaoId(UUID organizacaoId);
}
