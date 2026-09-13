package br.com.nsfatima.gestao.reserva.domain.repository;

import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída (Driven Port) para persistência e consulta do agregado Reserva.
 */
public interface ReservaRepository {

    Reserva save(Reserva reserva);

    List<Reserva> saveAll(List<Reserva> reservas);

    Optional<Reserva> findById(ReservaId id);

    /**
     * Busca com bloqueio pessimista reservas existentes ativas para o mesmo item que possam colidir com a janela informada.
     */
    List<Reserva> findConflitosPotenciaisComLock(
            TipoItemReserva tipoItem, UUID itemId, Instant inicioLimite, Instant fimLimite, UUID ignorarReservaId);

    List<Reserva> findPendentesExpiracao(Instant limiteDataHoraInicio);

    List<Reserva> findByEventoId(UUID eventoId);

    List<Reserva> findByGrupoRecorrenciaId(UUID grupoRecorrenciaId);

    List<Reserva> findAll(ReservaFiltro filtro);

    long countPendentesAprovacao(UUID pastoralId);

    long countDevolucoesPendentesHoje(Instant inicioHoje, Instant fimHoje);
}
