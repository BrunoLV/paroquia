package br.com.nsfatima.gestao.clero.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.SolicitacaoAjusteClericalEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas segregadas da fila de moderação clerical. */
public interface SolicitacaoAjusteClericalJpaRepository extends JpaRepository<SolicitacaoAjusteClericalEntity, UUID> {
    List<SolicitacaoAjusteClericalEntity> findByStatusOrderByCriadoEmUtcAsc(String status);
}
