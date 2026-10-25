package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.domain.model.StatusSolicitacaoDpo;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.SolicitacaoDpoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositório JPA para solicitações de titulares encaminhadas ao DPO. */
@Repository
public interface SolicitacaoDpoJpaRepository extends JpaRepository<SolicitacaoDpoEntity, UUID> {

    Optional<SolicitacaoDpoEntity> findByProtocolo(String protocolo);

    List<SolicitacaoDpoEntity> findAllByOrderByCriadoEmUtcDesc();

    List<SolicitacaoDpoEntity> findByStatusOrderByCriadoEmUtcDesc(StatusSolicitacaoDpo status);
}
