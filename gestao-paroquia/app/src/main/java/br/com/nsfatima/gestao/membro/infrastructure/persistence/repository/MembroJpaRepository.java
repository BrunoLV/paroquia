package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MembroJpaRepository extends JpaRepository<MembroEntity, UUID> {

    @Query("""
            SELECT m FROM MembroEntity m
            WHERE (:nome IS NULL OR LOWER(m.nomeCompleto) LIKE LOWER(CONCAT('%', :nome, '%')))
            AND (:ativo IS NULL OR m.ativo = :ativo)
            """)
    Page<MembroEntity> findByFiltros(
            @Param("nome") String nome,
            @Param("ativo") Boolean ativo,
            Pageable pageable);
}
