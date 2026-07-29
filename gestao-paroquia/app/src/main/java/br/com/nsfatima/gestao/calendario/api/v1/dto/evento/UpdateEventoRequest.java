package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Solicitação para atualização parcial de um evento")
public record UpdateEventoRequest(
        @Schema(description = "Novo título do evento")
        @Size(max = 160) @Pattern(regexp = ".*\\S.*", message = "titulo must not be blank if provided") String titulo,

        @Schema(description = "Nova descrição do evento")
        @Size(max = 4000) String descricao,

        @Schema(description = "Nova categoria do evento")
        CategoriaEvento categoria,

        @Schema(description = "Nova data de início")
        Instant inicio,

        @Schema(description = "Nova data de término")
        Instant fim,

        @Schema(description = "Novo status")
        EventoStatusInput status,

        @Schema(description = "Nova justificativa para adição extra")
        @Size(max = 4000) String adicionadoExtraJustificativa,

        @Schema(description = "Motivo do cancelamento")
        @Size(max = 2000) String canceladoMotivo,

        @Schema(description = "Novo ID da organização responsável")
        UUID organizacaoResponsavelId,

        @Schema(description = "Lista de IDs das organizações participantes")
        List<UUID> participantes,

        @Schema(description = "Escopo da edição (para eventos recorrentes)")
        EventoEditScope editScope,

        @Schema(description = "ID do projeto ao qual o evento pertence")
        UUID projetoId,

        @Schema(description = "ID do local onde o evento ocorrerá")
        UUID localId,

        @Schema(description = "ID da igreja de vínculo (Matriz ou Comunidade) se houver")
        UUID igrejaVinculoId,

        @Schema(description = "Flag de bypass para liberar conflito de agenda (apenas Pároco)")
        Boolean bypassAgendaConflict) {

    public UpdateEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            UUID organizacaoResponsavelId,
            List<UUID> participantes,
            EventoEditScope editScope,
            UUID projetoId,
            UUID localId) {
        this(titulo,
                descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                organizacaoResponsavelId, participantes, editScope, projetoId, localId, null, false);

    }

    public UpdateEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            UUID organizacaoResponsavelId,
            List<UUID> participantes,
            EventoEditScope editScope,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId) {
        this(titulo,
                descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                organizacaoResponsavelId, participantes, editScope, projetoId, localId, igrejaVinculoId, false);

    }

    public boolean isEmptyPayload() {

        return titulo == null
                && descricao == null
                && categoria == null
                && inicio == null
                && fim == null
                && status == null
                && adicionadoExtraJustificativa == null
                && canceladoMotivo == null
                && organizacaoResponsavelId == null
                && participantes == null
                && projetoId == null
                && localId == null
                && igrejaVinculoId == null
                && bypassAgendaConflict == null;
    }

    public boolean changesSensitiveFields() {
        return inicio != null || fim != null || canceladoMotivo != null || status == EventoStatusInput.CANCELADO ||
                projetoId != null || localId != null || igrejaVinculoId != null;
    }

    public boolean isBypassAgendaConflict() {
        return Boolean.TRUE.equals(bypassAgendaConflict);
    }
}
