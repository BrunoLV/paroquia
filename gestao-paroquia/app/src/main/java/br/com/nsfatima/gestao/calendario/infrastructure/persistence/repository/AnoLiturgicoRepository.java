package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface AnoLiturgicoRepository extends JpaRepository<AnoLiturgicoEntity, Integer> {

    @Query("""
           SELECT a FROM AnoLiturgicoEntity a
           WHERE :targetDate >= a.dataInicioAdvento AND :targetDate <= a.dataFimComum
           """)
    Optional<AnoLiturgicoEntity> findAnoLiturgicoByData(@Param("targetDate") Instant targetDate);
}
