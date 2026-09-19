package br.com.nsfatima.gestao.observabilidade.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para recebimento em lote (batching) de registros de logs e telemetria da UI.
 *
 * <p>O envio em lote evita sobrecarga de requisições HTTP na API, acumulando eventos
 * transitórios no cliente e descarregando-os de forma assíncrona.
 *
 * @param logs lista de registros de logs enviados pelo frontend
 */
@Schema(description = "Lote de logs e telemetria enviados pelo frontend")
public record LoteLogClienteRequest(
        @NotEmpty(message = "A lista de logs não pode estar vazia")
        @Size(max = 50, message = "O lote não pode exceder 50 registros por requisição")
        @Schema(description = "Lista de entradas de log a registrar")
        List<@Valid LogClienteDTO> logs
) {

    /**
     * Construtor compacto garantindo imutabilidade e lista não nula.
     */
    public LoteLogClienteRequest {
        logs = logs == null ? List.of() : List.copyOf(logs);
    }
}
