package br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.AtaHistoricoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Repositório Spring Data da trilha de auditoria das atas. */
public interface AtaHistoricoJpaRepository extends JpaRepository<AtaHistoricoEntity, UUID> {

    List<AtaHistoricoEntity> findByAtaIdOrderByOcorridoEmUtcAsc(UUID ataId);
}
