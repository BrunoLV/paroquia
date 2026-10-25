package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.DesafioConsentimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositório JPA para gestão dos desafios OTP de consentimento. */
@Repository
public interface DesafioConsentimentoJpaRepository extends JpaRepository<DesafioConsentimentoEntity, UUID> {

    Optional<DesafioConsentimentoEntity> findTopByTitularTipoAndTitularIdAndExpiraEmUtcAfterOrderByCriadoEmUtcDesc(
            TipoTitularConsentimento titularTipo, UUID titularId, Instant agora);

    List<DesafioConsentimentoEntity> findByTitularTipoAndTitularId(
            TipoTitularConsentimento titularTipo, UUID titularId);

    void deleteByExpiraEmUtcBefore(Instant agora);
}
