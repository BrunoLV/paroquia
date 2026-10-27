package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.ItemRef;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.PedidoReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
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
 *
 * <p>É a única porta pela qual um evento ocupa locais e ativos: as reservas nascem {@code CONFIRMADA} (RN-RES-03)
 * pelo mesmo {@link ReservaCriacaoService} das demais origens, portanto sujeitas a choque de agenda, manutenção
 * e elegibilidade do item. Solicitante, operador e pastoral são obrigatórios: não se inventa identidade para
 * uma reserva, pois ela é a trilha de quem ocupou o espaço.</p>
 */
@Service
public class EventoReservaIntegrationService {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ReservaRepository reservaRepository;
    private final ReservaCriacaoService criacaoService;
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public EventoReservaIntegrationService(
            ReservaRepository reservaRepository,
            ReservaCriacaoService criacaoService,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.criacaoService = Objects.requireNonNull(criacaoService, "ReservaCriacaoService é obrigatório.");
        this.publicadorReservaDeAtivo =
                Objects.requireNonNull(publicadorReservaDeAtivo, "PublicadorReservaDeAtivo é obrigatório.");
    }

    /** Dados do evento necessários para nomear e atribuir as reservas dele derivadas. */
    private record ContextoEvento(
            UUID eventoId,
            String titulo,
            UUID responsavelId,
            UUID pastoralId,
            List<TipoHorarioEvento> periodos,
            LocalDate data
    ) {
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
        ContextoEvento contexto = new ContextoEvento(
                eventoId, eventoTitulo, solicitanteId, pastoralId, periodos, inicio.atZone(BR_ZONE).toLocalDate());
        List<IntervaloReserva> intervalos = calcularIntervalosReservas(inicio, fim, periodos);

        List<PedidoReserva> pedidos = new ArrayList<>();
        for (IntervaloReserva intervalo : intervalos) {
            for (ItemRef item : itensDoEvento(localId, localIds, ativoIds)) {
                pedidos.add(pedidoDoEvento(contexto, item, intervalo));
            }
        }
        return pedidos.isEmpty() ? List.of() : criacaoService.criarTodas(pedidos);
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
        ContextoEvento contexto = new ContextoEvento(
                eventoId, eventoTitulo, operadorId, pastoralId, periodos, inicio.atZone(BR_ZONE).toLocalDate());
        List<ItemRef> itens = itensDoEvento(localId, localIds, ativoIds);
        List<Reserva> existentes = reservaRepository.findByEventoId(eventoId);
        List<IntervaloReserva> intervalos = calcularIntervalosReservas(inicio, fim, periodos);
        Instant agora = Instant.now();
        Set<UUID> ativosCancelados = new LinkedHashSet<>();

        criacaoService.bloquearItens(itens);

        for (Reserva r : existentes) {
            boolean mantido = itens.contains(new ItemRef(r.getTipoItem(), r.getItemId()));
            if (!mantido && r.getStatus().admiteCancelamento()) {
                cancelar(r, exigirOperador(operadorId), "Item desvinculado na edição do evento.", agora);
                registrarSeAtivo(r, ativosCancelados);
            }
        }

        for (ItemRef item : itens) {
            List<Reserva> editaveis = existentes.stream()
                    .filter(r -> r.getTipoItem() == item.tipoItem() && r.getItemId().equals(item.itemId())
                            && r.getStatus().admiteCancelamento())
                    .toList();
            for (int i = 0; i < intervalos.size(); i++) {
                if (i < editaveis.size()) {
                    criacaoService.reagendar(editaveis.get(i), intervalos.get(i), agora);
                } else {
                    criacaoService.criar(pedidoDoEvento(contexto, item, intervalos.get(i)));
                }
            }
            for (int i = intervalos.size(); i < editaveis.size(); i++) {
                Reserva sobra = editaveis.get(i);
                cancelar(sobra, exigirOperador(operadorId), "Turno removido na edição do evento.", agora);
                registrarSeAtivo(sobra, ativosCancelados);
            }
        }
        ativosCancelados.forEach(publicadorReservaDeAtivo::publicarParaAtivo);
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

        Set<UUID> ativosTocados = new LinkedHashSet<>();
        for (Reserva r : reservas) {
            if (r.getStatus().admiteCancelamento()) {
                cancelar(r, exigirOperador(operadorId), motivo, agora);
                registrarSeAtivo(r, ativosTocados);
            }
        }
        ativosTocados.forEach(publicadorReservaDeAtivo::publicarParaAtivo);
    }

    private void cancelar(Reserva reserva, UUID operadorId, String motivo, Instant agora) {
        reserva.cancelar(operadorId, motivo, false, agora);
        reservaRepository.save(reserva);
    }

    private void registrarSeAtivo(Reserva reserva, Collection<UUID> ativos) {
        if (reserva.getTipoItem() == TipoItemReserva.ATIVO) {
            ativos.add(reserva.getItemId());
        }
    }

    private UUID exigirOperador(UUID operadorId) {
        if (operadorId == null) {
            throw new ReservaOperacaoInvalidaException("A integração com o evento exige o operador identificado.");
        }
        return operadorId;
    }

    private List<ItemRef> itensDoEvento(UUID localId, List<UUID> localIds, List<UUID> ativoIds) {
        Set<UUID> locais = new LinkedHashSet<>();
        if (localIds != null) {
            locais.addAll(localIds);
        }
        locais.add(localId);
        locais.remove(null);
        Set<UUID> ativos = new LinkedHashSet<>();
        if (ativoIds != null) {
            ativos.addAll(ativoIds);
        }
        ativos.remove(null);

        List<ItemRef> itens = new ArrayList<>();
        locais.forEach(id -> itens.add(new ItemRef(TipoItemReserva.LOCAL, id)));
        ativos.forEach(id -> itens.add(new ItemRef(TipoItemReserva.ATIVO, id)));
        return itens;
    }

    private PedidoReserva pedidoDoEvento(ContextoEvento ctx, ItemRef item, IntervaloReserva intervalo) {
        String destino = item.tipoItem() == TipoItemReserva.LOCAL
                ? " vinculada ao evento: "
                : " de ativo para o evento: ";
        return new PedidoReserva(
                item.tipoItem(),
                item.itemId(),
                null,
                intervalo,
                "Reserva automática" + sufixoTurno(intervalo, ctx) + destino + ctx.titulo(),
                ctx.responsavelId(),
                ctx.pastoralId(),
                ctx.eventoId(),
                null,
                StatusReserva.CONFIRMADA
        );
    }

    private List<IntervaloReserva> calcularIntervalosReservas(
            Instant inicio,
            Instant fim,
            List<TipoHorarioEvento> periodos
    ) {
        if (periodos != null && !periodos.isEmpty()) {
            List<TipoHorarioEvento> turnos = periodos.stream()
                    .filter(Objects::nonNull)
                    .filter(TipoHorarioEvento::isPredefinido)
                    .distinct()
                    .toList();
            if (!turnos.isEmpty()) {
                LocalDate data = inicio.atZone(BR_ZONE).toLocalDate();
                List<IntervaloReserva> intervalos = new ArrayList<>();
                for (TipoHorarioEvento turno : turnos) {
                    var faixa = turno.calcularIntervalo(data, BR_ZONE);
                    intervalos.add(IntervaloReserva.de(faixa.inicioUtc(), faixa.fimUtc()));
                }
                return intervalos;
            }
        }
        return List.of(IntervaloReserva.de(inicio, fim));
    }

    private String sufixoTurno(IntervaloReserva intervalo, ContextoEvento ctx) {
        if (ctx.periodos() == null || ctx.periodos().isEmpty() || ctx.data() == null) {
            return "";
        }
        for (TipoHorarioEvento turno : ctx.periodos()) {
            if (turno != null && turno.isPredefinido()) {
                var faixa = turno.calcularIntervalo(ctx.data(), BR_ZONE);
                if (faixa.inicioUtc().equals(intervalo.dataHoraInicio())
                        && faixa.fimUtc().equals(intervalo.dataHoraFim())) {
                    return " (" + turno.getDescricao() + ")";
                }
            }
        }
        return "";
    }
}
