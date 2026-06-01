package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoEventoJpaRepository extends JpaRepository<ProjetoEventoEntity, UUID> {
    List<ProjetoEventoEntity> findByStatusAndInicioUtcLessThanEqual(String status, Instant date);
}

