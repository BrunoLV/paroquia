package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Dados de cadastro de um novo ativo patrimonial.
 *
 * @param reservavel indica se o ativo é elegível para reserva/empréstimo entre organizações
 *     (informativo no cadastro)
 */
public record CreateAtivoPatrimonialRequest(
        @NotBlank @Size(max = 200) String nome,
        @NotNull AtivoCategoria categoria,
        @NotNull UUID grupoParoquialId,
        boolean reservavel,
        LocalDate dataAquisicao,
        BigDecimal valorEstimado
) {
}
