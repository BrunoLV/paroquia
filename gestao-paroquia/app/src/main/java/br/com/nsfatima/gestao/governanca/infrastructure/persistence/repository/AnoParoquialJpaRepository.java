package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnoParoquialJpaRepository extends JpaRepository<AnoParoquialEntity, Integer> {
    Optional<AnoParoquialEntity> findByAno(Integer ano);

    boolean existsByAnoLessThanAndStatusNot(Integer ano, String status);
}
