package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoJpaRepository extends JpaRepository<EventoEntity, UUID> {

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
            select distinct e from EventoEntity e
            left join e.envolvidos ee
            where (:hasInicioUtc = false or e.inicioUtc >= :inicioUtc)
              and (:hasFimUtc = false or e.fimUtc <= :fimUtc)
              and (:hasOrganizacaoIds = false or e.organizacaoResponsavelId in :organizacaoIds)
              and (:hasProjetoId = false or e.projetoId = :projetoId)
              and (:hasEnvolvidoId = false or (e.organizacaoResponsavelId = :envolvidoId or ee.organizacaoId = :envolvidoId))
              and (:hasCategorias = false or e.categoria in :categorias)
              and (:hasStatuses = false or e.status in :statuses)
            """)
    Page<EventoEntity> findAllWithFilters(
            @Param("inicioUtc") Instant inicioUtc,
            @Param("hasInicioUtc") boolean hasInicioUtc,
            @Param("fimUtc") Instant fimUtc,
            @Param("hasFimUtc") boolean hasFimUtc,
            @Param("organizacaoIds") List<UUID> organizacaoIds,
            @Param("hasOrganizacaoIds") boolean hasOrganizacaoIds,
            @Param("projetoId") UUID projetoId,
            @Param("hasProjetoId") boolean hasProjetoId,
            @Param("envolvidoId") UUID envolvidoId,
            @Param("hasEnvolvidoId") boolean hasEnvolvidoId,
            @Param("categorias") List<String> categorias,
            @Param("hasCategorias") boolean hasCategorias,
            @Param("statuses") List<String> statuses,
            @Param("hasStatuses") boolean hasStatuses,
            Pageable pageable);

    List<EventoEntity> findAllByOrderByInicioUtcAscIdAsc();

    boolean existsByInicioUtcLessThanAndFimUtcGreaterThan(Instant fimUtcExclusive, Instant inicioUtcExclusive);

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

    boolean existsByLocalId(UUID localId);

    boolean existsByOrganizacaoResponsavelId(UUID organizacaoId);

    List<EventoEntity> findByRecorrenciaId(UUID recorrenciaId);

    @Query(value = """
            SELECT DISTINCT o.nome
            FROM (
                SELECT organizacao_responsavel_id as org_id FROM calendario.projetos_eventos WHERE id = :projetoId
                UNION
                SELECT organizacao_responsavel_id FROM calendario.eventos WHERE projeto_id = :projetoId
                UNION
                SELECT ee.organizacao_id FROM calendario.eventos_envolvidos ee
                JOIN calendario.eventos e ON ee.evento_id = e.id
                WHERE e.projeto_id = :projetoId
            ) involved
            JOIN calendario.organizacoes o ON involved.org_id = o.id
            """, nativeQuery = true)
    List<String> findInvolvedOrganizationNames(@Param("projetoId") UUID projetoId);
}
