package br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaOperacaoJpaRepository extends JpaRepository<AuditoriaOperacaoEntity, UUID> {

    @Query("""
            select a from AuditoriaOperacaoEntity a
            where a.organizacaoId = :organizacaoId
              and a.ocorridoEmUtc >= :inicioUtc
              and a.ocorridoEmUtc < :fimUtc
              and (:ator is null or a.ator = :ator)
              and (:acao is null or lower(a.acao) = lower(:acao))
              and (:resultado is null or lower(a.resultado) = lower(:resultado))
              and (:correlationId is null or a.correlationId = :correlationId)
            order by a.ocorridoEmUtc asc, a.id asc
            """)
    List<AuditoriaOperacaoEntity> findForTrail(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc,
            @Param("ator") String ator,
            @Param("acao") String acao,
            @Param("resultado") String resultado,
            @Param("correlationId") String correlationId);

    List<AuditoriaOperacaoEntity> findByOrganizacaoIdAndOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
            UUID organizacaoId,
            Instant inicioUtc,
            Instant fimUtc);

    List<AuditoriaOperacaoEntity> findByOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
            Instant inicioUtc,
            Instant fimUtc);

    @Query(value = """
            select count(*)
            from calendario.auditoria_operacoes a
            where a.organizacao_id = :organizacaoId
              and a.ocorrido_em_utc >= :inicioUtc
              and a.ocorrido_em_utc < :fimUtc
              and lower(a.resultado) in ('success', 'executed')
              and (
                    lower(a.acao) = 'cancel'
                    or (
                        lower(a.acao) = 'patch'
                        and (
                            a.detalhes_auditaveis_json like '%"scheduleChanged":true%'
                            or a.detalhes_auditaveis_json like '%"responsibleOrgChanged":true%'
                            or a.detalhes_auditaveis_json like '%"cancellation":true%'
                        )
                    )
              )
            """, nativeQuery = true)
    long countEligibleAdministrativeReworkOccurrences(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);

    @Query(value = """
            select count(distinct a.evento_id)
            from calendario.auditoria_operacoes a
            where a.organizacao_id = :organizacaoId
              and a.ocorrido_em_utc >= :inicioUtc
              and a.ocorrido_em_utc < :fimUtc
              and a.evento_id is not null
            """, nativeQuery = true)
    long countDistinctAffectedEvents(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);
}
