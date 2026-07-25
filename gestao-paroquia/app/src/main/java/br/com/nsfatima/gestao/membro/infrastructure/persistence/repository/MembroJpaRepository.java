package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembroJpaRepository extends JpaRepository<MembroEntity, UUID> {

    @Query("""
            SELECT m FROM MembroEntity m
            WHERE (:organizacaoId IS NULL OR m.organizacaoId = :organizacaoId)
            AND (:usuarioId IS NULL OR m.usuarioId = :usuarioId)
            AND (:ativo IS NULL OR m.ativo = :ativo)
            """)
    Page<MembroEntity> findByFiltros(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("usuarioId") UUID usuarioId,
            @Param("ativo") Boolean ativo,
            Pageable pageable);

    Optional<MembroEntity> findByUsuarioIdAndOrganizacaoId(UUID usuarioId, UUID organizacaoId);

    Optional<MembroEntity> findByUsuarioIdAndOrganizacaoIdAndAtivoTrue(UUID usuarioId, UUID organizacaoId);

    List<MembroEntity> findByUsuarioId(UUID usuarioId);

    List<MembroEntity> findByOrganizacaoId(UUID organizacaoId);
}
