package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Orquestra a integração transacional estrita entre o Módulo de Eventos e o Módulo de Reservas,
 * garantindo atomicidade, sincronização de alocações e cancelamento em cascata.
 */
@Service
public class EventoReservaIntegrationService {

    private final ReservaRepository reservaRepository;
    private final ReservaConflitoService conflitoService;
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public EventoReservaIntegrationService(
            ReservaRepository reservaRepository,
            ReservaConflitoService conflitoService,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.conflitoService = Objects.requireNonNull(conflitoService, "ReservaConflitoService é obrigatório.");
        this.publicadorReservaDeAtivo =
                Objects.requireNonNull(publicadorReservaDeAtivo, "PublicadorReservaDeAtivo é obrigatório.");
    }

    /** Notifica o contexto de patrimônio para recalcular o status RESERVADO de cada ativo tocado. */
    private void publicarAtivosTocados(Collection<Reserva> reservas) {
        Set<UUID> ativoIds = new LinkedHashSet<>();
        for (Reserva r : reservas) {
            if (r.getTipoItem() == TipoItemReserva.ATIVO) {
                ativoIds.add(r.getItemId());
            }
        }
        ativoIds.forEach(publicadorReservaDeAtivo::publicarParaAtivo);
    }

    @Transactional
    public List<Reserva> criarReservasEvento(
            UUID eventoId,
            String eventoTitulo,
            UUID localId,
            List<UUID> ativoIds,
            UUID solicitanteId,
            UUID pastoralId,
            Instant inicio,
            Instant fim
    ) {
        if (inicio == null || fim == null) {
            return List.of();
        }

        IntervaloReserva intervalo = IntervaloReserva.de(inicio, fim);
        List<Reserva> reservasParaSalvar = new ArrayList<>();
        Instant agora = Instant.now();

        // 1. Local
        if (localId != null) {
            conflitoService.validarDisponibilidadeComLock(
                    TipoItemReserva.LOCAL,
                    localId,
                    "Local do Evento",
                    intervalo,
                    null
            );

            reservasParaSalvar.add(Reserva.criar(
                    ReservaId.generate(),
                    TipoItemReserva.LOCAL,
                    localId,
                    intervalo,
                    "Reserva automática vinculada ao evento: " + eventoTitulo,
                    solicitanteId != null ? solicitanteId : UUID.randomUUID(),
                    pastoralId != null ? pastoralId : UUID.randomUUID(),
                    eventoId,
                    null,
                    StatusReserva.CONFIRMADA,
                    agora
            ));
        }

        // 2. Ativos
        if (ativoIds != null) {
            for (UUID ativoId : ativoIds) {
                if (ativoId == null) {
                    continue;
                }
                conflitoService.validarDisponibilidadeComLock(
                        TipoItemReserva.ATIVO,
                        ativoId,
                        "Ativo do Evento",
                        intervalo,
                        null
                );

                reservasParaSalvar.add(Reserva.criar(
                        ReservaId.generate(),
                        TipoItemReserva.ATIVO,
                        ativoId,
                        intervalo,
                        "Reserva automática de ativo para o evento: " + eventoTitulo,
                        solicitanteId != null ? solicitanteId : UUID.randomUUID(),
                        pastoralId != null ? pastoralId : UUID.randomUUID(),
                        eventoId,
                        null,
                        StatusReserva.CONFIRMADA,
                        agora
                ));
            }
        }

        if (reservasParaSalvar.isEmpty()) {
            return List.of();
        }
        List<Reserva> salvas = reservaRepository.saveAll(reservasParaSalvar);
        publicarAtivosTocados(salvas);
        return salvas;
    }

    @Transactional
    public void atualizarReservasEvento(
            UUID eventoId,
            String eventoTitulo,
            UUID localId,
            List<UUID> ativoIds,
            UUID operadorId,
            UUID pastoralId,
            Instant inicio,
            Instant fim
    ) {
        if (eventoId == null || inicio == null || fim == null) {
            return;
        }

        List<Reserva> reservasExistentes = reservaRepository.findByEventoId(eventoId);
        IntervaloReserva novoIntervalo = IntervaloReserva.de(inicio, fim);
        Instant agora = Instant.now();
        Set<UUID> ativosTocados = new LinkedHashSet<>();

        // Cancelar reservas de itens removidos
        for (Reserva r : reservasExistentes) {
            boolean manter = false;
            if (r.getTipoItem() == TipoItemReserva.LOCAL && Objects.equals(r.getItemId(), localId)) {
                manter = true;
            } else if (r.getTipoItem() == TipoItemReserva.ATIVO
                    && ativoIds != null && ativoIds.contains(r.getItemId())) {
                manter = true;
            }

            if (!manter && r.getStatus().ocupaAgenda()) {
                r.cancelar(operadorId != null ? operadorId : UUID.randomUUID(),
                        "Item desvinculado na edição do evento.", false, agora);
                reservaRepository.save(r);
                if (r.getTipoItem() == TipoItemReserva.ATIVO) {
                    ativosTocados.add(r.getItemId());
                }
            }
        }

        // Atualizar ou criar reservas para os itens atuais
        if (localId != null) {
            Reserva reservaLocal = reservasExistentes.stream()
                    .filter(r -> r.getTipoItem() == TipoItemReserva.LOCAL && Objects.equals(r.getItemId(), localId))
                    .findFirst()
                    .orElse(null);

            conflitoService.validarDisponibilidadeComLock(
                    TipoItemReserva.LOCAL,
                    localId,
                    "Local do Evento",
                    novoIntervalo,
                    reservaLocal != null ? reservaLocal.getId().value() : null
            );

            if (reservaLocal != null) {
                reservaLocal.atualizarIntervalo(novoIntervalo, agora);
                reservaRepository.save(reservaLocal);
            } else {
                Reserva nova = Reserva.criar(
                        ReservaId.generate(),
                        TipoItemReserva.LOCAL,
                        localId,
                        novoIntervalo,
                        "Reserva vinculada ao evento: " + eventoTitulo,
                        operadorId != null ? operadorId : UUID.randomUUID(),
                        pastoralId != null ? pastoralId : UUID.randomUUID(),
                        eventoId,
                        null,
                        StatusReserva.CONFIRMADA,
                        agora
                );
                reservaRepository.save(nova);
            }
        }

        if (ativoIds != null) {
            for (UUID ativoId : ativoIds) {
                if (ativoId == null) {
                    continue;
                }
                Reserva reservaAtivo = reservasExistentes.stream()
                        .filter(r -> r.getTipoItem() == TipoItemReserva.ATIVO && Objects.equals(r.getItemId(), ativoId))
                        .findFirst()
                        .orElse(null);

                conflitoService.validarDisponibilidadeComLock(
                        TipoItemReserva.ATIVO,
                        ativoId,
                        "Ativo do Evento",
                        novoIntervalo,
                        reservaAtivo != null ? reservaAtivo.getId().value() : null
                );

                if (reservaAtivo != null) {
                    reservaAtivo.atualizarIntervalo(novoIntervalo, agora);
                    reservaRepository.save(reservaAtivo);
                } else {
                    Reserva nova = Reserva.criar(
                            ReservaId.generate(),
                            TipoItemReserva.ATIVO,
                            ativoId,
                            novoIntervalo,
                            "Reserva de ativo vinculada ao evento: " + eventoTitulo,
                            operadorId != null ? operadorId : UUID.randomUUID(),
                            pastoralId != null ? pastoralId : UUID.randomUUID(),
                            eventoId,
                            null,
                            StatusReserva.CONFIRMADA,
                            agora
                    );
                    reservaRepository.save(nova);
                }
                ativosTocados.add(ativoId);
            }
        }

        ativosTocados.forEach(publicadorReservaDeAtivo::publicarParaAtivo);
    }

    @Transactional
    public void cancelarReservasEventoEmCascata(UUID eventoId, String eventoTitulo, UUID operadorId) {
        if (eventoId == null) {
            return;
        }
        List<Reserva> reservas = reservaRepository.findByEventoId(eventoId);
        Instant agora = Instant.now();
        String motivo = "Cancelamento automático decorrente do cancelamento do evento: "
                + (eventoTitulo != null ? eventoTitulo : "");

        for (Reserva r : reservas) {
            if (r.getStatus().ocupaAgenda()) {
                r.cancelar(operadorId != null ? operadorId : UUID.randomUUID(), motivo, false, agora);
                reservaRepository.save(r);
            }
        }
        publicarAtivosTocados(reservas);
    }
}
