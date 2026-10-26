package br.com.nsfatima.gestao.reserva.domain.service;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço de domínio responsável pela detecção rigorosa de sobreposições de agenda e buffers de logística.
 *
 * <p>A consulta ao repositório usa uma janela de busca ampliada pelo {@link IntervaloReserva#BUFFER_MAXIMO_MINUTOS}
 * em ambos os lados: como nenhuma margem logística excede esse teto, toda reserva que possa colidir está na janela
 * e a colisão exata é decidida em memória por {@link IntervaloReserva#colideCom(IntervaloReserva)}.</p>
 *
 * <p>A serialização de pedidos concorrentes não é feita aqui: o chamador deve ter bloqueado o item reservado
 * antes da consulta (ver {@code ReservaCriacaoService}).</p>
 */
public class ReservaConflitoService {

    private static final Duration MARGEM_DE_BUSCA = Duration.ofMinutes(IntervaloReserva.BUFFER_MAXIMO_MINUTOS);

    private final ReservaRepository reservaRepository;

    public ReservaConflitoService(ReservaRepository reservaRepository) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
    }

    /**
     * Valida se um item está livre na janela informada, lançando ReservaConflitoException se houver choque.
     */
    public void validarDisponibilidadeComLock(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId
    ) {
        validarDisponibilidadeComLock(tipoItem, itemId, itemNome, intervaloDesejado, ignorarReservaId, null);
    }

    /**
     * Variante que também desconsidera as reservas de um evento: os turnos de um mesmo evento são contíguos e a
     * margem logística de um não pode ser tratada como choque com o seguinte.
     */
    public void validarDisponibilidadeComLock(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId,
            UUID ignorarEventoId
    ) {
        List<ConflitoDetalhe> detalhes = detectarConflitos(
                tipoItem, itemId, itemNome, intervaloDesejado, ignorarReservaId, ignorarEventoId);
        if (!detalhes.isEmpty()) {
            throw new ReservaConflitoException(
                    "Identificado conflito de agenda ou período de preparação e desocupação para o recurso.", detalhes);
        }
    }

    /**
     * Identifica e compila as colisões para deliberação administrativa e pastoral, sem interromper
     * o fluxo com exceção impeditiva (409).
     */
    public List<ConflitoDetalhe> detectarConflitos(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId
    ) {
        return detectarConflitos(tipoItem, itemId, itemNome, intervaloDesejado, ignorarReservaId, null);
    }

    /** Variante de {@link #detectarConflitos} que desconsidera as reservas de um evento. */
    public List<ConflitoDetalhe> detectarConflitos(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId,
            UUID ignorarEventoId
    ) {
        if (itemId == null || intervaloDesejado == null) {
            return List.of();
        }
        List<Reserva> conflitos = colisoes(tipoItem, itemId, intervaloDesejado, ignorarReservaId, ignorarEventoId);
        List<ConflitoDetalhe> detalhes = new ArrayList<>();
        for (Reserva existente : conflitos) {
            detalhes.add(new ConflitoDetalhe(
                    itemId,
                    itemNome != null ? itemNome : (tipoItem == TipoItemReserva.LOCAL ? "Local " : "Ativo ") + itemId,
                    tipoItem,
                    existente.getIntervalo().dataHoraInicio(),
                    existente.getIntervalo().fimComBuffer(),
                    existente.getSolicitanteId(),
                    "Responsável da reserva",
                    existente.getDescricao()
            ));
        }
        return detalhes;
    }

    /**
     * Consulta no banco, com lock pessimista nas linhas encontradas, as reservas ativas que colidem com a janela.
     */
    public List<Reserva> buscarColisoesComLock(
            TipoItemReserva tipoItem,
            UUID itemId,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId
    ) {
        return colisoes(tipoItem, itemId, intervaloDesejado, ignorarReservaId, null);
    }

    private List<Reserva> colisoes(
            TipoItemReserva tipoItem,
            UUID itemId,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId,
            UUID ignorarEventoId
    ) {
        Instant inicioLimite = intervaloDesejado.dataHoraInicio().minus(MARGEM_DE_BUSCA);
        Instant fimLimite = intervaloDesejado.fimComBuffer().plus(MARGEM_DE_BUSCA);

        List<Reserva> candidatos = reservaRepository.findConflitosPotenciaisComLock(
                tipoItem,
                itemId,
                inicioLimite,
                fimLimite,
                ignorarReservaId
        );

        List<Reserva> colisoesEfetivas = new ArrayList<>();
        for (Reserva existente : candidatos) {
            boolean mesmoEvento = ignorarEventoId != null && ignorarEventoId.equals(existente.getEventoId());
            if (!mesmoEvento && existente.getStatus().ocupaAgenda()
                    && intervaloDesejado.colideCom(existente.getIntervalo())) {
                colisoesEfetivas.add(existente);
            }
        }
        return colisoesEfetivas;
    }
}
