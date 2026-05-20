package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnoParoquialJpaRepository extends JpaRepository<AnoParoquialEntity, Integer> {
    Optional<AnoParoquialEntity> findByAno(Integer ano);
}
