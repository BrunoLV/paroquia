package br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AprovacaoJpaRepository extends JpaRepository<AprovacaoEntity, UUID> {

    Optional<AprovacaoEntity> findByIdAndEventoId(UUID id, UUID eventoId);

    Optional<AprovacaoEntity> findByIdAndStatus(UUID id, AprovacaoStatus status);

    Optional<AprovacaoEntity> findTopByEventoIdAndTipoSolicitacaoAndStatusOrderByCriadoEmUtcDesc(
            UUID eventoId,
            String tipoSolicitacao,
            AprovacaoStatus status);

    boolean existsByEventoIdAndTipoSolicitacaoAndStatus(
            UUID eventoId,
            String tipoSolicitacao,
            AprovacaoStatus status);

    List<AprovacaoEntity>
    findByTipoSolicitacaoAndStatusOrderByCriadoEmUtcAsc(
            String tipoSolicitacao,
            AprovacaoStatus status);

    List<AprovacaoEntity>
    findByStatusOrderByCriadoEmUtcAsc(AprovacaoStatus status);

    @Query("SELECT a FROM AprovacaoEntity a WHERE " +
            "(:eventoId IS NULL OR a.eventoId = :eventoId) AND " +
            "(:status IS NULL OR a.status = :status)")
    Page<AprovacaoEntity> findByFilter(
            @Param("eventoId") UUID eventoId,
            @Param("status") AprovacaoStatus status,
            Pageable pageable);
}
