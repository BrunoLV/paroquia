package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.validation.ValidEventDates;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;

@ValidEventDates
@Schema(description = "Solicitação para criação de um novo evento")
public record CreateEventoRequest(
        @Schema(description = "Título do evento", example = "Missa de Domingo")
        @NotBlank @Size(max = 160) String titulo,

        @Schema(description = "Descrição detalhada do evento", example = "Celebração dominical na paróquia")
        @Size(max = 4000) String descricao,

        @Schema(description = "Categoria do evento")
        CategoriaEvento categoria,

        @Schema(description = "ID da organização responsável pelo evento")
        @NotNull UUID organizacaoResponsavelId,

        @Schema(description = "ID do projeto ao qual o evento pertence")
        UUID projetoId,

        @Schema(description = "Data e hora de início do evento (UTC)")
        @NotNull Instant inicio,

        @Schema(description = "Data e hora de término do evento (UTC)")
        @NotNull Instant fim,

        @Schema(description = "Status inicial do evento")
        EventoStatusInput status,

        @Schema(description = "Justificativa caso o evento seja adicionado extra-ordinariamente")
        String adicionadoExtraJustificativa,

        @Schema(description = "Lista de IDs das organizações participantes")
        List<UUID> participantes) {
}
