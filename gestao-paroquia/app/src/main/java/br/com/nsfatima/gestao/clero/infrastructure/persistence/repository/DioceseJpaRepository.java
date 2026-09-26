package br.com.nsfatima.gestao.clero.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.DioceseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DioceseJpaRepository extends JpaRepository<DioceseEntity, UUID> {

    Optional<DioceseEntity> findByNomeIgnoreCase(String nome);

    List<DioceseEntity> findByAtivoTrue();
}
