package br.com.nsfatima.gestao.reserva.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, UUID>, JpaSpecificationExecutor<ReservaEntity> {

    /**
     * Consulta pessimista com trava de escrita (PESSIMISTIC_WRITE) para serializar tentativas concorrentes
     * de alocação/aprovação para o mesmo item físico ou bem móvel.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM ReservaEntity r
        WHERE r.tipoItem = :tipoItem
          AND r.itemId = :itemId
          AND r.deletedAt IS NULL
          AND r.status NOT IN ('CANCELADA', 'EXPIRADA')
          AND r.dataHoraInicio < :fimLimite
          AND r.dataHoraFim > :inicioLimite
          AND (:ignorarReservaId IS NULL OR r.id <> :ignorarReservaId)
    """)
    List<ReservaEntity> findConflitosPotenciaisComLock(
            @Param("tipoItem") String tipoItem,
            @Param("itemId") UUID itemId,
            @Param("inicioLimite") Instant inicioLimite,
            @Param("fimLimite") Instant fimLimite,
            @Param("ignorarReservaId") UUID ignorarReservaId
    );

    @Query("""
        SELECT r FROM ReservaEntity r
        WHERE r.status = 'PENDENTE'
          AND r.deletedAt IS NULL
          AND r.dataHoraInicio <= :limite
    """)
    List<ReservaEntity> findPendentesExpiracao(@Param("limite") Instant limite);

    List<ReservaEntity> findByEventoIdAndDeletedAtIsNull(UUID eventoId);

    List<ReservaEntity> findByGrupoRecorrenciaIdAndDeletedAtIsNull(UUID grupoRecorrenciaId);

    @Query("""
        SELECT COUNT(r) FROM ReservaEntity r
        WHERE r.status = 'PENDENTE'
          AND r.deletedAt IS NULL
          AND (:pastoralId IS NULL OR r.pastoralMovimentoId = :pastoralId)
    """)
    long countPendentesAprovacao(@Param("pastoralId") UUID pastoralId);

    @Query("""
        SELECT COUNT(r) FROM ReservaEntity r
        WHERE r.tipoItem = 'ATIVO'
          AND r.status = 'RETIRADO'
          AND r.deletedAt IS NULL
          AND r.dataHoraFim <= :fimHoje
    """)
    long countDevolucoesPendentesHoje(@Param("fimHoje") Instant fimHoje);

    /**
     * Existe reserva de ativo com uso em vigor no instante informado (confirmada ou já retirada)?
     * Define o status {@code RESERVADO} do bem no contexto de patrimônio.
     */
    @Query("""
        SELECT (COUNT(r) > 0) FROM ReservaEntity r
        WHERE r.tipoItem = 'ATIVO'
          AND r.itemId = :ativoId
          AND r.deletedAt IS NULL
          AND r.status IN ('CONFIRMADA', 'RETIRADO')
          AND r.dataHoraInicio <= :agora
          AND r.dataHoraFim > :agora
    """)
    boolean existeReservaAtivaNoInstante(@Param("ativoId") UUID ativoId, @Param("agora") Instant agora);

    /**
     * Reservas PENDENTE/CONFIRMADA de um ativo que se sobrepõem à janela informada — impedem o
     * envio do bem para manutenção nesse período.
     */
    @Query("""
        SELECT r FROM ReservaEntity r
        WHERE r.tipoItem = 'ATIVO'
          AND r.itemId = :ativoId
          AND r.deletedAt IS NULL
          AND r.status IN ('PENDENTE', 'CONFIRMADA')
          AND r.dataHoraInicio < :fim
          AND r.dataHoraFim > :inicio
        ORDER BY r.dataHoraInicio ASC
    """)
    List<ReservaEntity> findImpeditivasParaManutencao(
            @Param("ativoId") UUID ativoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim);

    /** Existe reserva PENDENTE/CONFIRMADA de um ativo com início no futuro? Impede a exclusão do bem. */
    @Query("""
        SELECT (COUNT(r) > 0) FROM ReservaEntity r
        WHERE r.tipoItem = 'ATIVO'
          AND r.itemId = :ativoId
          AND r.deletedAt IS NULL
          AND r.status IN ('PENDENTE', 'CONFIRMADA')
          AND r.dataHoraInicio > :agora
    """)
    boolean existeReservaFuturaImpeditiva(@Param("ativoId") UUID ativoId, @Param("agora") Instant agora);
}
