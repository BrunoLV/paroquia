package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Sub-repositório de {@link EventoEntity} dedicado às consultas de leitura do calendário
 * (listagem paginada com filtros, eventos por período, sobreposição de horários e organizações
 * envolvidas em um projeto).
 *
 * <p>Faz parte da segregação por responsabilidade do agregado Evento (ver Javadoc de
 * {@link EventoJpaRepository} para o racional completo): esta interface concentra apenas as
 * consultas "de leitura pesada" usadas por telas de agenda/calendário, evitando misturar essa
 * responsabilidade com estatísticas ou validações de regra de negócio.
 */
@NoRepositoryBean
public interface EventoConsultaJpaRepository {

    /**
     * Busca eventos aplicando filtros opcionais combinados (período, organizações, projeto,
     * envolvido, categorias e status), paginando o resultado.
     *
     * <p>Cada filtro é ativado por um par de parâmetros {@code hasX}/{@code x}: quando o flag
     * {@code hasX} é {@code false}, o respectivo filtro é ignorado na consulta.
     *
     * @param inicioUtc limite inferior do início do evento (inclusive), quando o filtro estiver ativo
     * @param hasInicioUtc indica se o filtro de início deve ser aplicado
     * @param fimUtc limite superior do fim do evento (inclusive), quando o filtro estiver ativo
     * @param hasFimUtc indica se o filtro de fim deve ser aplicado
     * @param organizacaoIds identificadores de organizações responsáveis aceitos, quando o filtro estiver ativo
     * @param hasOrganizacaoIds indica se o filtro de organizações deve ser aplicado
     * @param projetoId identificador do projeto ao qual o evento pertence, quando o filtro estiver ativo
     * @param hasProjetoId indica se o filtro de projeto deve ser aplicado
     * @param envolvidoId identificador de organização envolvida (responsável ou participante), quando o filtro estiver
     *     ativo
     * @param hasEnvolvidoId indica se o filtro de envolvido deve ser aplicado
     * @param categorias categorias de evento aceitas, quando o filtro estiver ativo
     * @param hasCategorias indica se o filtro de categorias deve ser aplicado
     * @param statuses status de evento aceitos, quando o filtro estiver ativo
     * @param hasStatuses indica se o filtro de status deve ser aplicado
     * @param pageable configuração de paginação e ordenação
     * @return página de eventos que atendem aos filtros ativos
     */
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
            @Param("categorias") List<CategoriaEvento> categorias,
            @Param("hasCategorias") boolean hasCategorias,
            @Param("statuses") List<String> statuses,
            @Param("hasStatuses") boolean hasStatuses,
            Pageable pageable);

    /**
     * Lista todos os eventos ordenados por início e, em seguida, por identificador, para
     * garantir uma ordenação estável e determinística.
     *
     * @return todos os eventos cadastrados, ordenados por início e id ascendentes
     */
    List<EventoEntity> findAllByOrderByInicioUtcAscIdAsc();

    /**
     * Busca todas as ocorrências de eventos pertencentes a uma mesma série de recorrência.
     *
     * @param recorrenciaId identificador da recorrência (série) de eventos
     * @return eventos pertencentes à recorrência informada
     */
    List<EventoEntity> findByRecorrenciaId(UUID recorrenciaId);

    /**
     * Busca todos os eventos vinculados a um determinado projeto.
     *
     * @param projetoId identificador do projeto
     * @return eventos vinculados ao projeto informado
     */
    List<EventoEntity> findByProjetoId(UUID projetoId);

    /**
     * Busca (via consulta nativa) os nomes das organizações envolvidas em um projeto, seja como
     * organização responsável do próprio projeto, organização responsável de um evento do
     * projeto, ou organização participante (envolvida) de um evento do projeto.
     *
     * @param projetoId identificador do projeto
     * @return nomes distintos das organizações envolvidas no projeto informado
     */
    @Query(value = """
            SELECT DISTINCT COALESCE(gp.nome, ut.nome) as nome
            FROM (
                SELECT organizacao_responsavel_id as org_id FROM calendario.projetos_eventos WHERE id = :projetoId
                UNION
                SELECT organizacao_responsavel_id FROM calendario.eventos WHERE projeto_id = :projetoId
                UNION
                SELECT ee.organizacao_id FROM calendario.eventos_envolvidos ee
                JOIN calendario.eventos e ON ee.evento_id = e.id
                WHERE e.projeto_id = :projetoId
            ) involved
            LEFT JOIN calendario.organizacao gp ON involved.org_id = gp.id
            LEFT JOIN calendario.unidade_territorial ut ON involved.org_id = ut.id
            """, nativeQuery = true)
    List<String> findInvolvedOrganizationNames(@Param("projetoId") UUID projetoId);

    /**
     * Busca os eventos não cancelados cujo início ocorre dentro do período informado, ordenados
     * por início ascendente. Evento cancelado não ocupa a agenda e não entra nas métricas do painel.
     *
     * @param inicioUtc limite inferior (inclusive) do início do evento
     * @param fimUtc limite superior (inclusive) do início do evento
     * @return eventos não cancelados com início dentro do período, ordenados cronologicamente
     */
    @Query("""
            select distinct e from EventoEntity e
            where e.inicioUtc >= :inicioUtc and e.inicioUtc <= :fimUtc
              and e.status != 'CANCELADO'
            order by e.inicioUtc asc
            """)
    List<EventoEntity> findAllInPeriod(
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);

    /**
     * Busca eventos não cancelados cujo intervalo de tempo se sobrepõe ao período informado,
     * usada para validar conflitos de agenda antes de criar ou atualizar um evento.
     *
     * @param inicioUtc início do intervalo candidato a ser verificado
     * @param fimUtc fim do intervalo candidato a ser verificado
     * @param excludeId identificador de evento a ser excluído da verificação (o próprio evento
     *     em edição), ou {@code null} quando não há evento a excluir
     * @return eventos não cancelados que se sobrepõem ao período informado
     */
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

    /**
     * Busca eventos não cancelados sem projeto vinculado cujo início cai no período informado,
     * usado pelo indicador de "eventos órfãos" do dashboard. O intervalo é semiaberto
     * {@code [inicio, fim)} para casar com {@code JanelaTemporal}.
     *
     * @param inicioUtc limite inferior (inclusive) do início do evento
     * @param fimUtc limite superior (exclusive) do início do evento
     * @return eventos sem projeto no período, ordenados cronologicamente
     */
    @Query("""
            select e from EventoEntity e
            where e.projetoId is null
              and e.status != 'CANCELADO'
              and e.inicioUtc >= :inicioUtc and e.inicioUtc < :fimUtc
            order by e.inicioUtc asc
            """)
    List<EventoEntity> findOrfaosNoPeriodo(
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);

    /**
     * Busca o próximo evento não cancelado de um projeto ainda por ocorrer, priorizando o de
     * maior criticidade (menor {@code nivelPrioridade}) e, em empate, o mais próximo no tempo —
     * o "próximo marco crítico" exibido no card do projeto.
     *
     * @param projetoId identificador do projeto
     * @param agora instante de referência; só eventos que começam a partir dele são considerados
     * @return o próximo marco, ou {@link java.util.Optional#empty()} se não houver evento futuro
     */
    @Query("""
            select e from EventoEntity e
            where e.projetoId = :projetoId
              and e.status != 'CANCELADO'
              and e.inicioUtc >= :agora
            order by e.nivelPrioridade asc, e.inicioUtc asc
            limit 1
            """)
    Optional<EventoEntity> findProximoMarco(
            @Param("projetoId") UUID projetoId,
            @Param("agora") Instant agora);

    /**
     * Detecta pares de eventos não cancelados que dividem o mesmo local físico e se sobrepõem no
     * tempo, dentro do período informado — o indicador de choque de horário do dashboard. Cada
     * linha é {@code [localId (UUID), tituloA (String), tituloB (String), inicioA (Instant)]};
     * {@code a.id < b.id} garante que cada par apareça uma única vez.
     *
     * <p>É uma auto-junção O(eventos por local²): sempre chamada com uma janela temporal (nunca
     * global) para manter o custo baixo.
     *
     * @param inicioUtc limite inferior (inclusive) do início do primeiro evento do par
     * @param fimUtc limite superior (exclusive) do início do primeiro evento do par
     * @return pares em choque, ordenados por início
     */
    @Query("""
            select a.localId, a.titulo, b.titulo, a.inicioUtc
            from EventoEntity a, EventoEntity b
            where a.localId = b.localId and a.id < b.id
              and a.status != 'CANCELADO' and b.status != 'CANCELADO'
              and a.inicioUtc < b.fimUtc and b.inicioUtc < a.fimUtc
              and a.inicioUtc >= :inicioUtc and a.inicioUtc < :fimUtc
            order by a.inicioUtc asc
            """)
    List<Object[]> findChoquesHorarioPorLocal(
            @Param("inicioUtc") Instant inicioUtc,
            @Param("fimUtc") Instant fimUtc);
}
