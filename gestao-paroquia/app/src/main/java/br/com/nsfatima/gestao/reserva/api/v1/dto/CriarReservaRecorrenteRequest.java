package br.com.nsfatima.gestao.reserva.api.v1.dto;

import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.OcorrenciaHorario;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CriarReservaRecorrenteRequest(
        @NotNull(message = "O tipo do item é obrigatório.")
        TipoItemReserva tipoItem,

        @NotNull(message = "O identificador do item é obrigatório.")
        UUID itemId,

        String itemNome,

        Integer bufferMinutos,

        @NotBlank(message = "A descrição é obrigatória.")
        String descricao,

        @NotNull(message = "A pastoral/movimento é obrigatória.")
        UUID pastoralMovimentoId,

        @NotEmpty(message = "A lista de ocorrências é obrigatória.")
        List<OcorrenciaHorario> ocorrencias,

        boolean salvarValidasDescartarConflitos
) {}
