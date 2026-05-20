package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity.Key;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoEnvolvidoJpaRepository extends JpaRepository<EventoEnvolvidoEntity, Key> {

    List<EventoEnvolvidoEntity> findByEventoId(UUID eventoId);

    void deleteByEventoId(UUID eventoId);
}
