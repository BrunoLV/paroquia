package br.com.nsfatima.gestao.dashboard.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ContagemPorProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.EstatisticaEventosProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ReservaLocalIntervalo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Consultas de leitura do painel. Todas aceitam {@code organizacaoId} nulo como "visão global" e,
 * quando informado, restringem o resultado à organização do escopo, de modo que o recorte
 * organizacional é aplicado no banco e não por filtragem em memória de tabelas inteiras. Cada
 * consulta de projeto é agrupada por projeto para evitar N+1.
 */
public interface PainelConsultaJpaRepository extends Repository<EventoEntity, UUID> {

    /**
     * Projetos nos status informados, opcionalmente restritos à organização responsável.
     *
     * @param status status desejados
     * @param organizacaoId organização responsável, ou {@code null} para todas
     * @return projetos encontrados
     */
    @Query("""
            select p from ProjetoEventoEntity p
            where p.status in :status
              and (:organizacaoId is null or p.organizacaoResponsavelId = :organizacaoId)
            """)
    List<ProjetoEventoEntity> findProjetos(
            @Param("status") Collection<ProjetoStatus> status,
            @Param("organizacaoId") UUID organizacaoId);

    /**
     * Quantidade de projetos por status. Cada linha é {@code [status (ProjetoStatus), quantidade (Long)]}.
     *
     * @param organizacaoId organização responsável, ou {@code null} para todas
     * @return contagem por status
     */
    @Query("""
            select p.status, count(p) from ProjetoEventoEntity p
            where (:organizacaoId is null or p.organizacaoResponsavelId = :organizacaoId)
            group by p.status
            """)
    List<Object[]> contarProjetosPorStatus(@Param("organizacaoId") UUID organizacaoId);

    /**
     * Total, realizados e pendentes dos eventos de cada projeto, numa única consulta.
     *
     * @param projetoIds projetos de interesse
     * @param agora instante que separa realizados (término anterior) de pendentes
     * @return uma linha por projeto que tenha ao menos um evento
     */
    @Query("""
            select new br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.EstatisticaEventosProjeto(
                e.projetoId, count(e),
                sum(case when e.fimUtc < :agora then 1L else 0L end),
                sum(case when e.fimUtc >= :agora then 1L else 0L end))
            from EventoEntity e
            where e.projetoId in :projetoIds
            group by e.projetoId
            """)
    List<EstatisticaEventosProjeto> estatisticasEventosPorProjeto(
            @Param("projetoIds") Collection<UUID> projetoIds, @Param("agora") Instant agora);

    /**
     * Eventos futuros não cancelados dos projetos, do mais crítico (menor prioridade numérica) ao menos
     * crítico e, em empate, do mais próximo; o primeiro de cada projeto é o seu próximo marco.
     *
     * @param projetoIds projetos de interesse
     * @param agora instante a partir do qual o evento ainda está por ocorrer
     * @return eventos candidatos a marco, já ordenados
     */
    @Query("""
            select e from EventoEntity e
            where e.projetoId in :projetoIds
              and e.status != 'CANCELADO'
              and e.inicioUtc >= :agora
            order by e.nivelPrioridade asc, e.inicioUtc asc
            """)
    List<EventoEntity> eventosFuturosDosProjetos(
            @Param("projetoIds") Collection<UUID> projetoIds, @Param("agora") Instant agora);

    /**
     * Nomes das organizações envolvidas em cada projeto (responsável do projeto, responsável dos
     * eventos e participantes dos eventos). Cada linha é {@code [projetoId (UUID), nome (String)]}.
     *
     * @param projetoIds projetos de interesse
     * @return pares projeto/nome de organização, sem repetição
     */
    @Query(value = """
            SELECT DISTINCT involved.projeto_id, COALESCE(gp.nome, ut.nome) AS nome
            FROM (
                SELECT id AS projeto_id, organizacao_responsavel_id AS org_id
                FROM calendario.projetos_eventos WHERE id IN (:projetoIds)
                UNION
                SELECT projeto_id, organizacao_responsavel_id
                FROM calendario.eventos WHERE projeto_id IN (:projetoIds)
                UNION
                SELECT e.projeto_id, ee.organizacao_id
                FROM calendario.eventos_envolvidos ee
                JOIN calendario.eventos e ON ee.evento_id = e.id
                WHERE e.projeto_id IN (:projetoIds)
            ) involved
            LEFT JOIN calendario.organizacao gp ON involved.org_id = gp.id
            LEFT JOIN calendario.unidade_territorial ut ON involved.org_id = ut.id
            """, nativeQuery = true)
    List<Object[]> nomesOrganizacoesEnvolvidasPorProjeto(@Param("projetoIds") Collection<UUID> projetoIds);

    /**
     * Aprovações de evento no status informado, agrupadas pelo projeto do evento.
     *
     * @param projetoIds projetos de interesse
     * @param status status da aprovação (tipicamente {@code PENDENTE})
     * @return contagem por projeto
     */
    @Query("""
            select new br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ContagemPorProjeto(
                e.projetoId, count(a))
            from AprovacaoEntity a, EventoEntity e
            where a.eventoId = e.id and a.status = :status and e.projetoId in :projetoIds
            group by e.projetoId
            """)
    List<ContagemPorProjeto> contarAprovacoesPorProjeto(
            @Param("projetoIds") Collection<UUID> projetoIds, @Param("status") AprovacaoStatus status);

    /**
     * Aprovações de evento no status informado cujo evento pertence à organização do escopo.
     *
     * @param status status da aprovação
     * @param organizacaoId organização responsável pelo evento, ou {@code null} para todas
     * @return quantidade de aprovações
     */
    @Query("""
            select count(a) from AprovacaoEntity a
            where a.status = :status
              and (:organizacaoId is null or exists (
                  select 1 from EventoEntity e
                  where e.id = a.eventoId and e.organizacaoResponsavelId = :organizacaoId))
            """)
    long contarAprovacoesDeEvento(
            @Param("status") AprovacaoStatus status, @Param("organizacaoId") UUID organizacaoId);

    /**
     * Solicitações de projeto no status informado cujo projeto pertence à organização do escopo.
     *
     * @param status status da solicitação
     * @param organizacaoId organização responsável pelo projeto, ou {@code null} para todas
     * @return quantidade de solicitações
     */
    @Query("""
            select count(s) from ProjetoSolicitacaoAprovacaoEntity s
            where s.status = :status
              and (:organizacaoId is null or exists (
                  select 1 from ProjetoEventoEntity p
                  where p.id = s.projetoId and p.organizacaoResponsavelId = :organizacaoId))
            """)
    long contarSolicitacoesDeProjeto(
            @Param("status") AprovacaoStatus status, @Param("organizacaoId") UUID organizacaoId);

    /**
     * Aprovações de evento no status informado por tipo de solicitação, no escopo da organização.
     * Cada linha é {@code [tipoSolicitacao (String), quantidade (Long)]}.
     *
     * @param status status da aprovação
     * @param organizacaoId organização responsável pelo evento, ou {@code null} para todas
     * @return contagem por tipo
     */
    @Query("""
            select a.tipoSolicitacao, count(a) from AprovacaoEntity a
            where a.status = :status
              and (:organizacaoId is null or exists (
                  select 1 from EventoEntity e
                  where e.id = a.eventoId and e.organizacaoResponsavelId = :organizacaoId))
            group by a.tipoSolicitacao
            """)
    List<Object[]> contarAprovacoesDeEventoPorTipo(
            @Param("status") AprovacaoStatus status, @Param("organizacaoId") UUID organizacaoId);

    /**
     * Solicitações de projeto no status informado por tipo, no escopo da organização. Cada linha é
     * {@code [tipoSolicitacao (String), quantidade (Long)]}.
     *
     * @param status status da solicitação
     * @param organizacaoId organização responsável pelo projeto, ou {@code null} para todas
     * @return contagem por tipo
     */
    @Query("""
            select s.tipoSolicitacao, count(s) from ProjetoSolicitacaoAprovacaoEntity s
            where s.status = :status
              and (:organizacaoId is null or exists (
                  select 1 from ProjetoEventoEntity p
                  where p.id = s.projetoId and p.organizacaoResponsavelId = :organizacaoId))
            group by s.tipoSolicitacao
            """)
    List<Object[]> contarSolicitacoesDeProjetoPorTipo(
            @Param("status") AprovacaoStatus status, @Param("organizacaoId") UUID organizacaoId);

    /**
     * Fiéis ativos com vínculo ativo na organização.
     *
     * @param organizacaoId organização do escopo
     * @return fiéis distintos
     */
    @Query("""
            select count(distinct m.fielId) from MembroGrupoEntity m
            where m.ativo = true and m.organizacaoId = :organizacaoId
              and m.fielId in (select f.id from FielEntity f where f.ativo = true)
            """)
    long contarFieisAtivosDaOrganizacao(@Param("organizacaoId") UUID organizacaoId);

    /**
     * Fiéis ativos da organização cadastrados a partir do instante informado.
     *
     * @param organizacaoId organização do escopo
     * @param desde limite inferior (inclusive) do cadastro
     * @return fiéis distintos
     */
    @Query("""
            select count(distinct m.fielId) from MembroGrupoEntity m
            where m.ativo = true and m.organizacaoId = :organizacaoId
              and m.fielId in (select f.id from FielEntity f
                               where f.ativo = true and f.criadoEmUtc >= :desde)
            """)
    long contarFieisNovosDaOrganizacao(
            @Param("organizacaoId") UUID organizacaoId, @Param("desde") Instant desde);

    /**
     * Fiéis ativos da organização que também têm vínculo ativo em algum grupo dos tipos de engajamento.
     *
     * @param organizacaoId organização do escopo
     * @param tipos tipos de organização considerados engajamento (pastoral, movimento)
     * @return fiéis distintos
     */
    @Query("""
            select count(distinct m.fielId) from MembroGrupoEntity m
            where m.ativo = true and m.organizacaoId = :organizacaoId
              and m.fielId in (select f.id from FielEntity f where f.ativo = true)
              and exists (select 1 from MembroGrupoEntity m2
                          where m2.fielId = m.fielId and m2.ativo = true and m2.tipoOrganizacao in :tipos)
            """)
    long contarFieisEngajadosDaOrganizacao(
            @Param("organizacaoId") UUID organizacaoId, @Param("tipos") Collection<TipoOrganizacao> tipos);

    /**
     * Reservas de local que ocupam a agenda (todas exceto canceladas e expiradas) e tocam a janela,
     * opcionalmente restritas à organização que as solicitou (a organização responsável pelo evento).
     *
     * @param inicio início da janela
     * @param fim fim da janela
     * @param organizacaoId organização solicitante, ou {@code null} para todas
     * @return intervalos reservados por local
     */
    @Query("""
            select new br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ReservaLocalIntervalo(
                r.itemId, r.dataHoraInicio, r.dataHoraFim)
            from ReservaEntity r
            where r.tipoItem = 'LOCAL'
              and r.deletedAt is null
              and r.status not in ('CANCELADA', 'EXPIRADA')
              and r.dataHoraInicio < :fim and r.dataHoraFim > :inicio
              and (:organizacaoId is null or r.pastoralMovimentoId = :organizacaoId)
            """)
    List<ReservaLocalIntervalo> reservasDeLocalNaJanela(
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim,
            @Param("organizacaoId") UUID organizacaoId);
}
