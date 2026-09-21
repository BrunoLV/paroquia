package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private final LocalRepository localRepository;
    private final AtivoPatrimonialRepository ativoRepository;

    public EventoReservaIntegrationService(
            ReservaRepository reservaRepository,
            ReservaConflitoService conflitoService,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this(reservaRepository, conflitoService, publicadorReservaDeAtivo, null, null);
    }

    @Autowired
    public EventoReservaIntegrationService(
            ReservaRepository reservaRepository,
            ReservaConflitoService conflitoService,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo,
            @Autowired(required = false) LocalRepository localRepository,
            @Autowired(required = false) AtivoPatrimonialRepository ativoRepository
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.conflitoService = Objects.requireNonNull(conflitoService, "ReservaConflitoService é obrigatório.");
        this.publicadorReservaDeAtivo =
                Objects.requireNonNull(publicadorReservaDeAtivo, "PublicadorReservaDeAtivo é obrigatório.");
        this.localRepository = localRepository;
        this.ativoRepository = ativoRepository;
    }

    private String resolverNomeLocal(UUID localId) {
        if (localRepository != null && localId != null) {
            return localRepository.findById(localId).map(Local::getNome).orElse("Espaço Físico / Sala");
        }
        return "Espaço Físico / Sala";
    }

    private String resolverNomeAtivo(UUID ativoId) {
        if (ativoRepository != null && ativoId != null) {
            return ativoRepository.findById(new AtivoPatrimonialId(ativoId)).map(AtivoPatrimonial::getNome).orElse("Ativo Patrimonial");
        }
        return "Ativo Patrimonial";
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
        return criarReservasEvento(eventoId, eventoTitulo, localId,
                localId != null ? List.of(localId) : List.of(),
                ativoIds, solicitanteId, pastoralId, inicio, fim);
    }

    private List<IntervaloReserva> calcularIntervalosReservas(
            Instant inicio,
            Instant fim,
            List<TipoHorarioEvento> periodos
    ) {
        if (inicio == null || fim == null) {
            return List.of();
        }
        if (periodos != null && !periodos.isEmpty()) {
            List<TipoHorarioEvento> turnos = periodos.stream()
                    .filter(Objects::nonNull)
                    .filter(TipoHorarioEvento::isPredefinido)
                    .distinct()
                    .toList();
            if (!turnos.isEmpty()) {
                LocalDate data = inicio.atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate();
                List<IntervaloReserva> intervalos = new ArrayList<>();
                for (TipoHorarioEvento turno : turnos) {
                    var faixa = turno.calcularIntervalo(data, ZoneId.of("America/Sao_Paulo"));
                    intervalos.add(IntervaloReserva.de(faixa.inicioUtc(), faixa.fimUtc()));
                }
                return intervalos;
            }
        }
        return List.of(IntervaloReserva.de(inicio, fim));
    }

    private String extrairSufixoTurno(IntervaloReserva intervalo, List<TipoHorarioEvento> periodos, LocalDate data) {
        if (periodos == null || periodos.isEmpty() || data == null || intervalo == null) {
            return "";
        }
        for (TipoHorarioEvento turno : periodos) {
            if (turno != null && turno.isPredefinido()) {
                var faixa = turno.calcularIntervalo(data, ZoneId.of("America/Sao_Paulo"));
                if (faixa.inicioUtc().equals(intervalo.dataHoraInicio()) && faixa.fimUtc().equals(intervalo.dataHoraFim())) {
                    return " (" + turno.getDescricao() + ")";
                }
            }
        }
        return "";
    }

    @Transactional
    public List<Reserva> criarReservasEvento(
            UUID eventoId,
            String eventoTitulo,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            UUID solicitanteId,
            UUID pastoralId,
            Instant inicio,
            Instant fim
    ) {
        return criarReservasEvento(eventoId, eventoTitulo, localId, localIds, ativoIds, solicitanteId, pastoralId, inicio, fim, null);
    }

    @Transactional
    public List<Reserva> criarReservasEvento(
            UUID eventoId,
            String eventoTitulo,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            UUID solicitanteId,
            UUID pastoralId,
            Instant inicio,
            Instant fim,
            List<TipoHorarioEvento> periodos
    ) {
        if (inicio == null || fim == null) {
            return List.of();
        }

        List<IntervaloReserva> intervalos = calcularIntervalosReservas(inicio, fim, periodos);
        List<Reserva> reservasParaSalvar = new ArrayList<>();
        Instant agora = Instant.now();

        Set<UUID> todosLocais = new LinkedHashSet<>();
        if (localIds != null) {
            todosLocais.addAll(localIds);
        }
        if (localId != null) {
            todosLocais.add(localId);
        }

        LocalDate dataEvento = inicio.atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate();

        for (IntervaloReserva intervalo : intervalos) {
            String sufixoTurno = extrairSufixoTurno(intervalo, periodos, dataEvento);

            // 1. Locais
            for (UUID locId : todosLocais) {
                if (locId == null) {
                    continue;
                }
                conflitoService.validarDisponibilidadeComLock(
                        TipoItemReserva.LOCAL,
                        locId,
                        resolverNomeLocal(locId),
                        intervalo,
                        null
                );

                reservasParaSalvar.add(Reserva.criar(
                        ReservaId.gerar(),
                        TipoItemReserva.LOCAL,
                        locId,
                        intervalo,
                        "Reserva automática" + sufixoTurno + " vinculada ao evento: " + eventoTitulo,
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
                            resolverNomeAtivo(ativoId),
                            intervalo,
                            null
                    );

                    reservasParaSalvar.add(Reserva.criar(
                            ReservaId.gerar(),
                            TipoItemReserva.ATIVO,
                            ativoId,
                            intervalo,
                            "Reserva automática" + sufixoTurno + " de ativo para o evento: " + eventoTitulo,
                            solicitanteId != null ? solicitanteId : UUID.randomUUID(),
                            pastoralId != null ? pastoralId : UUID.randomUUID(),
                            eventoId,
                            null,
                            StatusReserva.CONFIRMADA,
                            agora
                    ));
                }
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
        atualizarReservasEvento(eventoId, eventoTitulo, localId,
                localId != null ? List.of(localId) : List.of(),
                ativoIds, operadorId, pastoralId, inicio, fim);
    }

    @Transactional
    public void atualizarReservasEvento(
            UUID eventoId,
            String eventoTitulo,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            UUID operadorId,
            UUID pastoralId,
            Instant inicio,
            Instant fim
    ) {
        atualizarReservasEvento(eventoId, eventoTitulo, localId, localIds, ativoIds, operadorId, pastoralId, inicio, fim, null);
    }

    @Transactional
    public void atualizarReservasEvento(
            UUID eventoId,
            String eventoTitulo,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            UUID operadorId,
            UUID pastoralId,
            Instant inicio,
            Instant fim,
            List<TipoHorarioEvento> periodos
    ) {
        if (eventoId == null || inicio == null || fim == null) {
            return;
        }

        List<Reserva> reservasExistentes = reservaRepository.findByEventoId(eventoId);
        List<IntervaloReserva> novosIntervalos = calcularIntervalosReservas(inicio, fim, periodos);
        Instant agora = Instant.now();
        Set<UUID> ativosTocados = new LinkedHashSet<>();

        Set<UUID> todosLocais = new LinkedHashSet<>();
        if (localIds != null) {
            todosLocais.addAll(localIds);
        }
        if (localId != null) {
            todosLocais.add(localId);
        }

        // Cancelar reservas de itens desvinculados
        for (Reserva r : reservasExistentes) {
            boolean manterItem = false;
            if (r.getTipoItem() == TipoItemReserva.LOCAL && todosLocais.contains(r.getItemId())) {
                manterItem = true;
            } else if (r.getTipoItem() == TipoItemReserva.ATIVO
                    && ativoIds != null && ativoIds.contains(r.getItemId())) {
                manterItem = true;
            }

            if (!manterItem && r.getStatus().ocupaAgenda()) {
                r.cancelar(operadorId != null ? operadorId : UUID.randomUUID(),
                        "Item desvinculado na edição do evento.", false, agora);
                reservaRepository.save(r);
                if (r.getTipoItem() == TipoItemReserva.ATIVO) {
                    ativosTocados.add(r.getItemId());
                }
            }
        }

        LocalDate dataEvento = inicio.atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate();

        // Alinhar reservas de locais com os novos intervalos
        for (UUID locId : todosLocais) {
            if (locId == null) {
                continue;
            }
            List<Reserva> ativasDoItem = reservasExistentes.stream()
                    .filter(r -> r.getTipoItem() == TipoItemReserva.LOCAL
                            && Objects.equals(r.getItemId(), locId)
                            && r.getStatus().ocupaAgenda())
                    .toList();

            for (int i = 0; i < novosIntervalos.size(); i++) {
                IntervaloReserva intervalo = novosIntervalos.get(i);
                String sufixoTurno = extrairSufixoTurno(intervalo, periodos, dataEvento);
                if (i < ativasDoItem.size()) {
                    Reserva r = ativasDoItem.get(i);
                    conflitoService.validarDisponibilidadeComLock(
                            TipoItemReserva.LOCAL,
                            locId,
                            resolverNomeLocal(locId),
                            intervalo,
                            r.getId().value()
                    );
                    r.atualizarIntervalo(intervalo, agora);
                    reservaRepository.save(r);
                } else {
                    conflitoService.validarDisponibilidadeComLock(
                            TipoItemReserva.LOCAL,
                            locId,
                            resolverNomeLocal(locId),
                            intervalo,
                            null
                    );
                    Reserva nova = Reserva.criar(
                            ReservaId.gerar(),
                            TipoItemReserva.LOCAL,
                            locId,
                            intervalo,
                            "Reserva vinculada ao evento" + sufixoTurno + ": " + eventoTitulo,
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

            for (int i = novosIntervalos.size(); i < ativasDoItem.size(); i++) {
                Reserva r = ativasDoItem.get(i);
                r.cancelar(operadorId != null ? operadorId : UUID.randomUUID(),
                        "Turno de local removido na edição do evento.", false, agora);
                reservaRepository.save(r);
            }
        }

        // Alinhar reservas de ativos com os novos intervalos
        if (ativoIds != null) {
            for (UUID ativoId : ativoIds) {
                if (ativoId == null) {
                    continue;
                }
                List<Reserva> ativasDoItem = reservasExistentes.stream()
                        .filter(r -> r.getTipoItem() == TipoItemReserva.ATIVO
                                && Objects.equals(r.getItemId(), ativoId)
                                && r.getStatus().ocupaAgenda())
                        .toList();

                for (int i = 0; i < novosIntervalos.size(); i++) {
                    IntervaloReserva intervalo = novosIntervalos.get(i);
                    String sufixoTurno = extrairSufixoTurno(intervalo, periodos, dataEvento);
                    if (i < ativasDoItem.size()) {
                        Reserva r = ativasDoItem.get(i);
                        conflitoService.validarDisponibilidadeComLock(
                                TipoItemReserva.ATIVO,
                                ativoId,
                                resolverNomeAtivo(ativoId),
                                intervalo,
                                r.getId().value()
                        );
                        r.atualizarIntervalo(intervalo, agora);
                        reservaRepository.save(r);
                    } else {
                        conflitoService.validarDisponibilidadeComLock(
                                TipoItemReserva.ATIVO,
                                ativoId,
                                resolverNomeAtivo(ativoId),
                                intervalo,
                                null
                        );
                        Reserva nova = Reserva.criar(
                                ReservaId.gerar(),
                                TipoItemReserva.ATIVO,
                                ativoId,
                                intervalo,
                                "Reserva de ativo vinculada ao evento" + sufixoTurno + ": " + eventoTitulo,
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

                for (int i = novosIntervalos.size(); i < ativasDoItem.size(); i++) {
                    Reserva r = ativasDoItem.get(i);
                    r.cancelar(operadorId != null ? operadorId : UUID.randomUUID(),
                            "Turno de ativo removido na edição do evento.", false, agora);
                    reservaRepository.save(r);
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
