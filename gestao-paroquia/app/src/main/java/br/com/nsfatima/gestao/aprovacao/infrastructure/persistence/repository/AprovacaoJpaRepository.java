package br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AprovacaoJpaRepository extends JpaRepository<AprovacaoEntity, UUID> {

        Optional<AprovacaoEntity> findByIdAndEventoId(UUID id, UUID eventoId);

        Optional<AprovacaoEntity> findByIdAndStatusIgnoreCase(UUID id, String status);

        Optional<AprovacaoEntity> findTopByEventoIdAndTipoSolicitacaoAndStatusIgnoreCaseOrderByCriadoEmUtcDesc(
                        UUID eventoId,
                        String tipoSolicitacao,
                        String status);

        boolean existsByEventoIdAndTipoSolicitacaoAndStatusIgnoreCase(
                        UUID eventoId,
                        String tipoSolicitacao,
                        String status);

        List<AprovacaoEntity>
 findByTipoSolicitacaoAndStatusIgnoreCaseOrderByCriadoEmUtcAsc(
                        String tipoSolicitacao,
                        String status);

        List<AprovacaoEntity>
 findByStatusIgnoreCaseOrderByCriadoEmUtcAsc(String status);

        @Query("SELECT a FROM AprovacaoEntity a WHERE " +
                        "(:eventoId IS NULL OR a.eventoId = :eventoId) AND " +
                        "(:status IS NULL OR LOWER(a.status) = LOWER(:status))")
        Page<AprovacaoEntity> findByFilter(
                        @Param("eventoId") UUID eventoId,
                        @Param("status") String status,
                        Pageable pageable);
}
