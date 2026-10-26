package br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA de {@link AprovacaoEntity}, oferecendo as consultas usadas pelo fluxo de aprovação: busca de
 * solicitações pendentes/decididas por evento e tipo, verificação de duplicidade e listagem filtrada/paginada.
 */
public interface AprovacaoJpaRepository extends JpaRepository<AprovacaoEntity, UUID> {

    /**
     * Busca uma aprovação pelo par (id, eventoId), garantindo que a aprovação pertence ao evento informado.
     *
     * @param id       identificador da aprovação
     * @param eventoId identificador do evento associado
     * @return aprovação correspondente, se existir
     */
    Optional<AprovacaoEntity> findByIdAndEventoId(UUID id, UUID eventoId);

    /**
     * Busca uma aprovação pelo id, restrita a um status específico — usado para validar pré-condições antes
     * de operar sobre a solicitação.
     *
     * @param id     identificador da aprovação
     * @param status status esperado da aprovação
     * @return aprovação correspondente, se existir e estiver no status informado
     */
    Optional<AprovacaoEntity> findByIdAndStatus(UUID id, AprovacaoStatus status);

    /**
     * Busca a aprovação mais recente de um evento para um tipo de solicitação e status, usada para recuperar
     * a última decisão relevante (ex.: última aprovação pendente de um tipo).
     *
     * @param eventoId        identificador do evento
     * @param tipoSolicitacao tipo da solicitação de aprovação
     * @param status          status da aprovação
     * @return aprovação mais recente que satisfaz os critérios, se existir
     */
    Optional<AprovacaoEntity> findTopByEventoIdAndTipoSolicitacaoAndStatusOrderByCriadoEmUtcDesc(
            UUID eventoId,
            String tipoSolicitacao,
            AprovacaoStatus status);

    /**
     * Verifica se já existe uma aprovação para o evento, tipo e status informados — usado para evitar a
     * criação de solicitações de aprovação duplicadas (ex.: já existe uma PENDENTE do mesmo tipo).
     *
     * @param eventoId        identificador do evento
     * @param tipoSolicitacao tipo da solicitação de aprovação
     * @param status          status da aprovação
     * @return {@code true} se já existir uma aprovação correspondente
     */
    boolean existsByEventoIdAndTipoSolicitacaoAndStatus(
            UUID eventoId,
            String tipoSolicitacao,
            AprovacaoStatus status);

    /**
     * Lista, em ordem cronológica de criação, as aprovações de um determinado tipo e status — usado, por
     * exemplo, para varrer solicitações pendentes de um tipo específico.
     *
     * @param tipoSolicitacao tipo da solicitação de aprovação
     * @param status          status da aprovação
     * @return aprovações correspondentes, ordenadas da mais antiga para a mais recente
     */
    List<AprovacaoEntity>
    findByTipoSolicitacaoAndStatusOrderByCriadoEmUtcAsc(
            String tipoSolicitacao,
            AprovacaoStatus status);

    /**
     * Lista, em ordem cronológica de criação, todas as aprovações de um determinado status (ex.: todas as
     * PENDENTES, para processamento em lote ou expiração).
     *
     * @param status status da aprovação
     * @return aprovações correspondentes, ordenadas da mais antiga para a mais recente
     */
    List<AprovacaoEntity>
    findByStatusOrderByCriadoEmUtcAsc(AprovacaoStatus status);

    /**
     * Lista de forma paginada as aprovações, filtrando opcionalmente por evento e/ou status (filtros nulos são
     * ignorados) — usado pela listagem administrativa de solicitações de aprovação.
     *
     * @param eventoId identificador do evento a filtrar; {@code null} não filtra por evento
     * @param status   status a filtrar; {@code null} não filtra por status
     * @param pageable parâmetros de paginação/ordenação
     * @return página de aprovações que satisfazem os filtros informados
     */
    @Query("SELECT a FROM AprovacaoEntity a WHERE " +
            "a.aprovacaoPaiId IS NULL AND " +
            "(:eventoId IS NULL OR a.eventoId = :eventoId) AND " +
            "(:status IS NULL OR a.status = :status)")
    Page<AprovacaoEntity> findByFilter(
            @Param("eventoId") UUID eventoId,
            @Param("status") AprovacaoStatus status,
            Pageable pageable);

    /**
     * Mesma listagem de {@link #findByFilter}, restringível ao solicitante: com {@code solicitanteId} informado
     * só devolve as solicitações abertas por ele (visão de quem propõe, mas não decide).
     *
     * @param eventoId identificador do evento a filtrar; {@code null} não filtra
     * @param status status a filtrar; {@code null} não filtra
     * @param solicitanteId identificador do solicitante; {@code null} não restringe
     * @param pageable parâmetros de paginação/ordenação
     * @return página de aprovações que satisfazem os filtros
     */
    @Query("SELECT a FROM AprovacaoEntity a WHERE " +
            "a.aprovacaoPaiId IS NULL AND " +
            "(:eventoId IS NULL OR a.eventoId = :eventoId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:solicitanteId IS NULL OR a.solicitanteId = :solicitanteId)")
    Page<AprovacaoEntity> findByFilterDoSolicitante(
            @Param("eventoId") UUID eventoId,
            @Param("status") AprovacaoStatus status,
            @Param("solicitanteId") String solicitanteId,
            Pageable pageable);

    /**
     * Conta as aprovações de evento em um status, agrupadas por tipo de solicitação — o
     * detalhamento de pendências de homologação do dashboard. Cada linha é
     * {@code [tipoSolicitacao (String), quantidade (long)]}.
     *
     * @param status status a considerar (tipicamente {@link AprovacaoStatus#PENDENTE})
     * @return contagem por tipo de solicitação
     */
    @Query("SELECT a.tipoSolicitacao, count(a) FROM AprovacaoEntity a WHERE a.status = :status "
            + "GROUP BY a.tipoSolicitacao")
    List<Object[]> contarPorTipoComStatus(@Param("status") AprovacaoStatus status);

    /**
     * Conta a quantidade total de aprovações com um determinado status (ex.: PENDENTE),
     * alimentando métricas em tempo real e gauges do Micrometer.
     *
     * @param status status da aprovação
     * @return quantidade total encontrada
     */
    long countByStatus(AprovacaoStatus status);

    /**
     * Lista todas as ocorrências filhas de uma aprovação mestre ordenadas pela data da ocorrência.
     *
     * @param aprovacaoPaiId ID da aprovação mestre
     * @return lista de ocorrências filhas
     */
    List<AprovacaoEntity> findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(UUID aprovacaoPaiId);

    /**
     * Lista as ocorrências filhas de uma aprovação mestre filtradas por status.
     *
     * @param aprovacaoPaiId ID da aprovação mestre
     * @param status status da aprovação filha
     * @return lista de ocorrências filhas correspondentes
     */
    List<AprovacaoEntity> findByAprovacaoPaiIdAndStatus(UUID aprovacaoPaiId, AprovacaoStatus status);

    /**
     * Conta a quantidade total de ocorrências filhas vinculadas a uma aprovação mestre.
     *
     * @param aprovacaoPaiId ID da aprovação mestre
     * @return total de ocorrências filhas
     */
    long countByAprovacaoPaiId(UUID aprovacaoPaiId);

    /**
     * Lista todas as aprovações associadas a um determinado evento.
     *
     * @param eventoId ID do evento
     * @return lista de aprovações
     */
    List<AprovacaoEntity> findByEventoId(UUID eventoId);
}

