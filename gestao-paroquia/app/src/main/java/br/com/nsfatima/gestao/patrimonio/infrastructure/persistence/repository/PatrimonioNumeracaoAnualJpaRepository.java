package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.PatrimonioNumeracaoAnualEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatrimonioNumeracaoAnualJpaRepository extends JpaRepository<PatrimonioNumeracaoAnualEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PatrimonioNumeracaoAnualEntity> findById(Integer ano);
}
