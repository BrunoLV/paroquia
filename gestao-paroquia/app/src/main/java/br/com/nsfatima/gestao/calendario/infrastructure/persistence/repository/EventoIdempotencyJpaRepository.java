package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoIdempotencyJpaRepository extends JpaRepository<EventoIdempotencyEntity, String> {
}
