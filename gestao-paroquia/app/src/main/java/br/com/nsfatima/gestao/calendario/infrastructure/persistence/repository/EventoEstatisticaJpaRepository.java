package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface EventoEstatisticaJpaRepository {

    @Query("select e.status from EventoEntity e where e.id = :id")
    Optional<String> findStatusByIdNoLock(@Param("id") UUID id);

    @Query("""
            select count(distinct e.id) from EventoEntity e
            where e.organizacaoResponsavelId = :organizacaoId
              and e.id in :eventoIds
            """)
    long countDistinctByOrganizacaoResponsavelIdAndIdIn(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("eventoIds") List<UUID> eventoIds);

    @Query("""
            select count(e) from EventoEntity e
            where e.organizacaoResponsavelId = :organizacaoId
              and e.status = :status
              and e.inicioUtc >= :inicio
              and e.inicioUtc < :fim
            """)
    long countByOrganizacaoAndStatusAndPeriod(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("status") String status,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    @Query("""
            select count(e) from EventoEntity e
            where e.organizacaoResponsavelId = :organizacaoId
              and e.inicioUtc >= :inicio
              and e.inicioUtc < :fim
            """)
    long countByOrganizacaoAndPeriod(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    long countByProjetoId(UUID projetoId);

    long countByProjetoIdAndStatus(UUID projetoId, String status);

    long countByProjetoIdAndFimUtcLessThan(UUID projetoId, Instant now);

    long countByProjetoIdAndFimUtcGreaterThanEqual(UUID projetoId, Instant now);
}
