package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EventoJpaRepository extends JpaRepository<EventoEntity, UUID>,
        EventoConsultaJpaRepository, EventoEstatisticaJpaRepository, EventoValidacaoJpaRepository {
}
