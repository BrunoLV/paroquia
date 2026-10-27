package br.com.nsfatima.gestao.reserva.api.v1.dto;

import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Pedido público de reserva. Não carrega vínculo com evento nem série recorrente: esses vínculos são
 * estabelecidos apenas pela integração com eventos e pelo endpoint recorrente, nunca pelo cliente, pois
 * conferiam confirmação automática da reserva.
 */
public record CriarReservaRequest(
        @NotNull(message = "O tipo do item (LOCAL ou ATIVO) é obrigatório.")
        TipoItemReserva tipoItem,

        @NotNull(message = "O identificador do item reservado é obrigatório.")
        UUID itemId,

        String itemNome,

        @NotNull(message = "A data/hora de início é obrigatória.")
        Instant dataHoraInicio,

        @NotNull(message = "A data/hora de término é obrigatória.")
        Instant dataHoraFim,

        Integer bufferMinutos,

        @NotBlank(message = "A descrição da finalidade é obrigatória.")
        String descricao,

        @NotNull(message = "A pastoral/movimento solicitante é obrigatória.")
        UUID pastoralMovimentoId
) {}
