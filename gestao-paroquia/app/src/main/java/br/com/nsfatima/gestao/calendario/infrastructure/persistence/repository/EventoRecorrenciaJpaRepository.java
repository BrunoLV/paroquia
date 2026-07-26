package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoRecorrenciaJpaRepository extends JpaRepository<EventoRecorrenciaEntity, UUID> {
}
