package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório Spring Data para {@link GrupoParoquialEntity}, combinando o CRUD genérico com as consultas
 * customizadas de {@link GrupoParoquialConsultaJpaRepository}.
 */
@Repository
public interface GrupoParoquialJpaRepository extends JpaRepository<GrupoParoquialEntity, UUID>,
        GrupoParoquialConsultaJpaRepository {
}
