package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.IncidenteSegurancaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidenteSegurancaJpaRepository extends JpaRepository<IncidenteSegurancaEntity, UUID> {
    List<IncidenteSegurancaEntity> findAllByOrderByDataDeteccaoDesc();
}
