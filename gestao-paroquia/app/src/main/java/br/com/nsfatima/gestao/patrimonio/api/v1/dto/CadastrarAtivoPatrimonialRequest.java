package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de cadastro de um novo ativo patrimonial. O código de tombamento é gerado pelo sistema e
 * nunca informado; categoria (tipo do bem), data e valor de aquisição são obrigatórios;
 * {@code descricao} é opcional.
 */
public record CadastrarAtivoPatrimonialRequest(
        @NotBlank @Size(max = 200) String nome,
        @Size(max = 4000) String descricao,
        @NotNull AtivoCategoria categoria,
        @NotNull UUID organizacaoId,
        boolean isReservavel,
        @NotNull LocalDate dataAquisicao,
        @NotNull @PositiveOrZero BigDecimal valorAquisicao
) {
}
