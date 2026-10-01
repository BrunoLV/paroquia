package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoHistoricoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Repositório Spring Data da trilha de histórico do assento. */
public interface AssentoHistoricoJpaRepository extends JpaRepository<AssentoHistoricoEntity, UUID> {

    List<AssentoHistoricoEntity> findByAssentoIdOrderByOcorridoEmUtcAsc(UUID assentoId);
}
