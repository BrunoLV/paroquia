package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoManutencaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Acesso a persistência das manutenções de ativos patrimoniais. */
@Repository
public interface AtivoManutencaoJpaRepository extends JpaRepository<AtivoManutencaoEntity, UUID> {

    Optional<AtivoManutencaoEntity> findFirstByAtivoIdAndDataFimRealIsNull(UUID ativoId);

    boolean existsByAtivoIdAndDataFimRealIsNull(UUID ativoId);

    List<AtivoManutencaoEntity> findByAtivoIdOrderByDataInicioDesc(UUID ativoId);

    @Query("""
            SELECT m FROM AtivoManutencaoEntity m
            WHERE m.ativoId = :ativoId
              AND (m.dataFimReal IS NULL
                   OR (m.dataInicio < :fim AND m.dataFimPrevista > :inicio))
            """)
    List<AtivoManutencaoEntity> findJanelasBloqueantes(
            @Param("ativoId") UUID ativoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);
}
