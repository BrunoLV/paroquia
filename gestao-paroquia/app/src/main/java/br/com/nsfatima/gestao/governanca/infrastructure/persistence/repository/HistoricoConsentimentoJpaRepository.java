package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.HistoricoConsentimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Repositório JPA para trilha imutável de auditoria de consentimento LGPD. */
@Repository
public interface HistoricoConsentimentoJpaRepository extends JpaRepository<HistoricoConsentimentoEntity, UUID> {

    List<HistoricoConsentimentoEntity> findByTitularTipoAndTitularIdOrderByCriadoEmUtcDesc(
            TipoTitularConsentimento titularTipo, UUID titularId);
}
