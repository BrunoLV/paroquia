package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import java.util.UUID;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRecorrenciaJpaRepository extends JpaRepository<EventoRecorrenciaEntity, UUID> {
}
