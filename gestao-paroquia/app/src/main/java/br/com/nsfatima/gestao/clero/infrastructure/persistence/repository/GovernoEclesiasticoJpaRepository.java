package br.com.nsfatima.gestao.clero.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.GovernoEclesiasticoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GovernoEclesiasticoJpaRepository extends JpaRepository<GovernoEclesiasticoEntity, UUID> {

    Optional<GovernoEclesiasticoEntity> findFirstByOrderByCriadoEmUtcDesc();
}
