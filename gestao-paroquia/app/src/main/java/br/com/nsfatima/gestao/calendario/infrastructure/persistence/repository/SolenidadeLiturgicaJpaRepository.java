package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.SolenidadeLiturgicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolenidadeLiturgicaJpaRepository extends JpaRepository<SolenidadeLiturgicaEntity, UUID> {
    List<SolenidadeLiturgicaEntity> findByAtivoTrue();
    Optional<SolenidadeLiturgicaEntity> findByChave(String chave);
}
