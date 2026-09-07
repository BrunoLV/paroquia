package br.com.nsfatima.gestao.clero.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas segregadas do agregado Clerigo. */
public interface ClerigoJpaRepository extends JpaRepository<ClerigoEntity, UUID> {
    List<ClerigoEntity> findByAtivoTrueOrderByNomeEclesiasticoAsc();
}
