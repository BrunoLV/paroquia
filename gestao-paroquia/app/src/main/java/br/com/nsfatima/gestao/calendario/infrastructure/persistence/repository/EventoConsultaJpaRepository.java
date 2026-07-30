package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface EventoConsultaJpaRepository {

    @Query("""
            select distinct e from EventoEntity e
            left join e.envolvidos ee
            where (:hasInicioUtc = false or e.inicioUtc >= :inicioUtc)
              and (:hasFimUtc = false or e.fimUtc <= :fimUtc)
              and (:hasOrganizacaoIds = false or e.organizacaoResponsavelId in :organizacaoIds)
              and (:hasProjetoId = false or e.projetoId = :projetoId)
              and (:hasEnvolvidoId = false\s
                          or (e.organizacaoResponsavelId = :envolvidoId or ee.organizacaoId = :envolvidoId))
              and (:hasCategorias = false or e.categoria in :categorias)
              and (:hasStatuses = false or e.status in :statuses)
            \s""")
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

    List<EventoEntity> findByRecorrenciaId(UUID recorrenciaId);

    List<EventoEntity> findByProjetoId(UUID projetoId);

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

    @Query("""
            select distinct e from EventoEntity e
            where e.inicioUtc >= :inicioUtc and e.inicioUtc <= :fimUtc
            order by e.inicioUtc asc
            """)
    List<EventoEntity> findAllInPeriod(
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);

    @Query("""
            select e from EventoEntity e
            where e.status != 'CANCELADO'
              and (:excludeId is null or e.id != :excludeId)
              and e.inicioUtc < :fimUtc
              and e.fimUtc > :inicioUtc
            """)
    List<EventoEntity> findOverlappingEvents(
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc,
            @Param("excludeId") UUID excludeId);
}
