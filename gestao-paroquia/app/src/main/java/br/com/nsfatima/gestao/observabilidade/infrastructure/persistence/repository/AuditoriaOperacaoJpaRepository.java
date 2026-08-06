package br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Acesso a dados para a trilha de auditoria ({@link AuditoriaOperacaoEntity}). Além da consulta
 * genérica de trilha, expõe consultas nativas de suporte a indicadores de retrabalho/qualidade
 * (ex.: taxa de eventos que sofreram cancelamento ou alteração administrativa relevante) usadas
 * pelos casos de uso de métricas do contexto {@code calendario}.
 */
public interface AuditoriaOperacaoJpaRepository extends JpaRepository<AuditoriaOperacaoEntity, UUID> {

    /**
     * Busca registros de auditoria de uma organização em um intervalo de tempo, aplicando
     * filtros opcionais (qualquer parâmetro nulo é ignorado na cláusula {@code where}),
     * ordenados cronologicamente para permitir reconstrução sequencial dos eventos.
     *
     * @param grupoParoquialId organização cujo histórico será consultado
     * @param inicioUtc limite inferior (inclusivo) do intervalo, em UTC
     * @param fimUtc limite superior (exclusivo) do intervalo, em UTC
     * @param ator filtro opcional por ator
     * @param acao filtro opcional por ação (case-insensitive)
     * @param resultado filtro opcional por resultado (case-insensitive)
     * @param correlationId filtro opcional para agrupar registros da mesma requisição/lote
     * @return registros de auditoria correspondentes, ordenados por data e id ascendentes
     */
    @Query("""
            select a from AuditoriaOperacaoEntity a
            where a.grupoParoquialId = :grupoParoquialId
              and a.ocorridoEmUtc >= :inicioUtc
              and a.ocorridoEmUtc < :fimUtc
              and (:ator is null or a.ator = :ator)
              and (:acao is null or lower(a.acao) = lower(:acao))
              and (:resultado is null or lower(a.resultado) = lower(:resultado))
              and (:correlationId is null or a.correlationId = :correlationId)
            order by a.ocorridoEmUtc asc, a.id asc
            """)
    List<AuditoriaOperacaoEntity> findForTrail(
            @Param("grupoParoquialId") UUID grupoParoquialId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc,
            @Param("ator") String ator,
            @Param("acao") String acao,
            @Param("resultado") String resultado,
            @Param("correlationId") String correlationId);

    /**
     * Busca todos os registros de auditoria de uma organização em um intervalo de tempo, sem
     * filtros adicionais, ordenados cronologicamente. Usado como base para cálculo de
     * indicadores agregados (ex.: retrabalho) que precisam varrer o histórico completo do
     * período, não apenas um subconjunto filtrado.
     *
     * @param grupoParoquialId organização cujo histórico será consultado
     * @param inicioUtc limite inferior (inclusivo) do intervalo, em UTC
     * @param fimUtc limite superior (exclusivo) do intervalo, em UTC
     * @return registros de auditoria da organização no período, ordenados por data e id
     */
    List<AuditoriaOperacaoEntity>
    findByGrupoParoquialIdAndOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
            UUID grupoParoquialId,
            Instant inicioUtc,
            Instant fimUtc);

    /**
     * Busca todos os registros de auditoria de qualquer organização em um intervalo de tempo,
     * usado por indicadores calculados no nível global/paróquia (não escopados a uma única
     * organização).
     *
     * @param inicioUtc limite inferior (inclusivo) do intervalo, em UTC
     * @param fimUtc limite superior (exclusivo) do intervalo, em UTC
     * @return registros de auditoria no período, ordenados por data e id
     */
    List<AuditoriaOperacaoEntity>
    findByOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
            Instant inicioUtc,
            Instant fimUtc);

    /**
     * Conta ocorrências elegíveis para o indicador de retrabalho administrativo: operações bem
     * sucedidas ("sucesso"/"executado") que representam ou um cancelamento de evento, ou uma
     * atualização que alterou agenda, organização responsável ou sinalizou cancelamento no
     * JSON de detalhes ({@code agendaAlterada}, {@code grupoParoquialResponsavelAlterada},
     * {@code cancelamento}). Essa regra materializa, em SQL nativo por performance, o critério
     * de negócio de "retrabalho" usado por {@code ReworkRateCalculator}.
     *
     * @param grupoParoquialId organização cujo indicador será calculado
     * @param inicioUtc limite inferior (inclusivo) do intervalo, em UTC
     * @param fimUtc limite superior (exclusivo) do intervalo, em UTC
     * @return quantidade de ocorrências elegíveis de retrabalho no período
     */
    @Query(value = """
            select count(*)
            from calendario.auditoria_operacoes a
            where a.grupo_paroquial_id = :grupoParoquialId
              and a.ocorrido_em_utc >= :inicioUtc
              and a.ocorrido_em_utc < :fimUtc
              and lower(a.resultado) in ('sucesso', 'executado')
              and (
                    lower(a.acao) = 'cancelar'
                    or (
                        lower(a.acao) = 'atualizar'
                        and (
                            a.detalhes_auditaveis_json like '%"agendaAlterada":true%'
                            or a.detalhes_auditaveis_json like '%"grupoParoquialResponsavelAlterada":true%'
                            or a.detalhes_auditaveis_json like '%"cancelamento":true%'
                        )
                    )
              )
            """, nativeQuery = true)
    long countEligibleAdministrativeReworkOccurrences(
            @Param("grupoParoquialId") UUID grupoParoquialId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);

    /**
     * Conta a quantidade de eventos distintos afetados por alguma operação auditável no
     * período, usada como denominador de indicadores de taxa (ex.: retrabalho) que precisam
     * normalizar pela base de eventos realmente movimentados, não pelo total de eventos
     * existentes.
     *
     * @param grupoParoquialId organização cujo indicador será calculado
     * @param inicioUtc limite inferior (inclusivo) do intervalo, em UTC
     * @param fimUtc limite superior (exclusivo) do intervalo, em UTC
     * @return quantidade de eventos distintos com ao menos um registro de auditoria no período
     */
    @Query(value = """
            select count(distinct a.evento_id)
            from calendario.auditoria_operacoes a
            where a.grupo_paroquial_id = :grupoParoquialId
              and a.ocorrido_em_utc >= :inicioUtc
              and a.ocorrido_em_utc < :fimUtc
              and a.evento_id is not null
            """, nativeQuery = true)
    long countDistinctAffectedEvents(
            @Param("grupoParoquialId") UUID grupoParoquialId,
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);
}
