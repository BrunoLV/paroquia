package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Sub-repositório de {@link br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity}
 * dedicado a consultas agregadas e estatísticas (contagens por organização, status, período e
 * projeto), usadas por métricas e indicadores.
 *
 * <p>Faz parte da segregação por responsabilidade do agregado Evento (ver Javadoc de
 * {@link EventoJpaRepository} para o racional completo): esta interface concentra apenas
 * consultas de agregação/contagem, mantendo-as separadas das consultas de leitura de calendário
 * e das consultas de validação de regra de negócio.
 */
@NoRepositoryBean
public interface EventoEstatisticaJpaRepository {

    /**
     * Busca o status atual de um evento sem adquirir lock de leitura/escrita, útil em contextos
     * de leitura de métricas onde uma leitura suja é aceitável.
     *
     * @param id identificador do evento
     * @return o status do evento, ou {@link Optional#empty()} caso o evento não exista
     */
    @Query("select e.status from EventoEntity e where e.id = :id")
    Optional<String> findStatusByIdNoLock(@Param("id") UUID id);

    /**
     * Conta quantos eventos distintos, dentre os identificadores informados, pertencem a uma
     * determinada organização responsável.
     *
     * @param grupoParoquialId identificador da organização responsável
     * @param eventoIds identificadores de eventos candidatos a serem contados
     * @return quantidade de eventos distintos da organização informada dentre os ids informados
     */
    @Query("""
            select count(distinct e.id) from EventoEntity e
            where e.grupoParoquialResponsavelId = :grupoParoquialId
              and e.id in :eventoIds
            """)
    long countDistinctByGrupoParoquialResponsavelIdAndIdIn(
            @Param("grupoParoquialId") UUID grupoParoquialId,
            @Param("eventoIds") List<UUID> eventoIds);

    /**
     * Conta eventos de uma organização responsável, filtrando por status e por um período de
     * início.
     *
     * @param grupoParoquialId identificador da organização responsável
     * @param status status do evento a ser considerado na contagem
     * @param inicio limite inferior (inclusive) do início do evento
     * @param fim limite superior (exclusive) do início do evento
     * @return quantidade de eventos que atendem aos critérios informados
     */
    @Query("""
            select count(e) from EventoEntity e
            where e.grupoParoquialResponsavelId = :grupoParoquialId
              and e.status = :status
              and e.inicioUtc >= :inicio
              and e.inicioUtc < :fim
            """)
    long countByGrupoParoquialAndStatusAndPeriod(
            @Param("grupoParoquialId") UUID grupoParoquialId,
            @Param("status") String status,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    /**
     * Conta eventos de uma organização responsável dentro de um período de início, sem filtro de
     * status.
     *
     * @param grupoParoquialId identificador da organização responsável
     * @param inicio limite inferior (inclusive) do início do evento
     * @param fim limite superior (exclusive) do início do evento
     * @return quantidade de eventos que atendem aos critérios informados
     */
    @Query("""
            select count(e) from EventoEntity e
            where e.grupoParoquialResponsavelId = :grupoParoquialId
              and e.inicioUtc >= :inicio
              and e.inicioUtc < :fim
            """)
    long countByGrupoParoquialAndPeriod(
            @Param("grupoParoquialId") UUID grupoParoquialId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    /**
     * Conta todos os eventos vinculados a um determinado projeto.
     *
     * @param projetoId identificador do projeto
     * @return quantidade de eventos vinculados ao projeto informado
     */
    long countByProjetoId(UUID projetoId);

    /**
     * Conta os eventos de um projeto que estão em um determinado status.
     *
     * @param projetoId identificador do projeto
     * @param status status do evento a ser considerado na contagem
     * @return quantidade de eventos do projeto no status informado
     */
    long countByProjetoIdAndStatus(UUID projetoId, String status);

    /**
     * Conta os eventos de um projeto cujo fim já ocorreu antes do instante informado (eventos já
     * encerrados).
     *
     * @param projetoId identificador do projeto
     * @param now instante de referência para considerar um evento como encerrado
     * @return quantidade de eventos do projeto já encerrados antes do instante informado
     */
    long countByProjetoIdAndFimUtcLessThan(UUID projetoId, Instant now);

    /**
     * Conta os eventos de um projeto cujo fim ainda não ocorreu no instante informado (eventos
     * em andamento ou futuros).
     *
     * @param projetoId identificador do projeto
     * @param now instante de referência para considerar um evento como não encerrado
     * @return quantidade de eventos do projeto não encerrados até o instante informado
     */
    long countByProjetoIdAndFimUtcGreaterThanEqual(UUID projetoId, Instant now);
}
