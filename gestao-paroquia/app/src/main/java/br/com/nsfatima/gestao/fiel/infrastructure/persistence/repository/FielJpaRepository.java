package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório JPA de {@link FielEntity}.
 */
@Repository
public interface FielJpaRepository extends JpaRepository<FielEntity, UUID> {
}
