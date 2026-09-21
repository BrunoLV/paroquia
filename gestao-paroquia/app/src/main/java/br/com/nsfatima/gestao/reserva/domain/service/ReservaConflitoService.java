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
 */
public class ReservaConflitoService {

    private final ReservaRepository reservaRepository;

    public ReservaConflitoService(ReservaRepository reservaRepository) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
    }

    /**
     * Valida se um item está livre na janela informada com bloqueio pessimista, lançando ReservaConflitoException se houver choque.
     */
    public void validarDisponibilidadeComLock(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId
    ) {
        List<Reserva> conflitos = buscarColisoesComLock(tipoItem, itemId, intervaloDesejado, ignorarReservaId);

        if (!conflitos.isEmpty()) {
            List<ConflitoDetalhe> detalhes = new ArrayList<>();
            for (Reserva existente : conflitos) {
                detalhes.add(new ConflitoDetalhe(
                        itemId,
                        itemNome != null ? itemNome : "Recurso " + itemId,
                        tipoItem,
                        existente.getIntervalo().dataHoraInicio(),
                        existente.getIntervalo().fimComBuffer(),
                        existente.getSolicitanteId(),
                        "Responsável da reserva",
                        existente.getDescricao()
                ));
            }
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
        if (itemId == null || intervaloDesejado == null) {
            return List.of();
        }
        List<Reserva> conflitos = buscarColisoesComLock(tipoItem, itemId, intervaloDesejado, ignorarReservaId);
        if (conflitos.isEmpty()) {
            return List.of();
        }
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
     * Consulta no banco com lock pessimista as reservas ativas que colidem com a janela informada.
     */
    public List<Reserva> buscarColisoesComLock(
            TipoItemReserva tipoItem,
            UUID itemId,
            IntervaloReserva intervaloDesejado,
            UUID ignorarReservaId
    ) {
        // Limites amplos para a query pessimista, cobrindo o buffer máximo (ex: 2 horas de margem de segurança)
        Instant inicioLimite = intervaloDesejado.dataHoraInicio().minus(Duration.ofHours(4));
        Instant fimLimite = intervaloDesejado.fimComBuffer().plus(Duration.ofHours(4));

        List<Reserva> candidatos = reservaRepository.findConflitosPotenciaisComLock(
                tipoItem,
                itemId,
                inicioLimite,
                fimLimite,
                ignorarReservaId
        );

        List<Reserva> colisoesEfetivas = new ArrayList<>();
        for (Reserva existente : candidatos) {
            if (existente.getStatus().ocupaAgenda() && intervaloDesejado.colideCom(existente.getIntervalo())) {
                colisoesEfetivas.add(existente);
            }
        }
        return colisoesEfetivas;
    }
}
