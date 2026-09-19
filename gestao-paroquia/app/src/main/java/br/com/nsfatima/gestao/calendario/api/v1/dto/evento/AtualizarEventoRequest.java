package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Solicitação para atualização parcial de um evento: todos os campos são opcionais e apenas os
 * informados (não nulos) substituem o valor atual do evento. Alterações em campos sensíveis
 * (agenda, cancelamento, projeto, local, igreja de vínculo) podem exigir reaprovação, conforme
 * verificado por {@link #alteraCamposSensiveis()}.
 */
@Schema(description = "Solicitação para atualização parcial de um evento")
public record AtualizarEventoRequest(
        @Schema(description = "Novo título do evento")
        @Size(max = 160) @Pattern(regexp = ".*\\S.*", message = "titulo não deve ser vazio quando informado") String titulo,

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
        UUID grupoParoquialResponsavelId,

        @Schema(description = "Lista de IDs das organizações participantes")
        List<UUID> participantes,

        @Schema(description = "Escopo da edição (para eventos recorrentes)")
        EscopoEdicaoEvento escopoEdicao,

        @Schema(description = "ID do projeto ao qual o evento pertence")
        UUID projetoId,

        @Schema(description = "ID do local onde o evento ocorrerá")
        UUID localId,

        @Schema(description = "ID da igreja de vínculo (Matriz ou Comunidade) se houver")
        UUID igrejaVinculoId,

        @Schema(description = "Flag para liberar conflito de agenda (apenas Pároco)")
        Boolean liberarConflitoAgenda,

        @Schema(description = "Lista de IDs dos ativos patrimoniais alocados para o evento")
        List<UUID> ativoIds) {

    public AtualizarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            UUID grupoParoquialResponsavelId,
            List<UUID> participantes,
            EscopoEdicaoEvento escopoEdicao,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Boolean liberarConflitoAgenda) {
        this(titulo, descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                grupoParoquialResponsavelId, participantes, escopoEdicao, projetoId, localId, igrejaVinculoId,
                liberarConflitoAgenda, null);
    }

    public AtualizarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            UUID grupoParoquialResponsavelId,
            List<UUID> participantes,
            EscopoEdicaoEvento escopoEdicao,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId) {
        this(titulo, descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                grupoParoquialResponsavelId, participantes, escopoEdicao, projetoId, localId, igrejaVinculoId, false);
    }

    public AtualizarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            UUID grupoParoquialResponsavelId,
            List<UUID> participantes,
            EscopoEdicaoEvento escopoEdicao,
            UUID projetoId,
            UUID localId) {
        this(titulo,
                descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                grupoParoquialResponsavelId, participantes, escopoEdicao, projetoId, localId, null, false);
    }

    /**
     * Indica se nenhum campo de atualização foi informado.
     *
     * @return {@code true} se todos os campos de atualização forem {@code null}
     */
    public boolean isPayloadVazio() {
        return titulo == null
                && descricao == null
                && categoria == null
                && inicio == null
                && fim == null
                && status == null
                && adicionadoExtraJustificativa == null
                && canceladoMotivo == null
                && grupoParoquialResponsavelId == null
                && participantes == null
                && projetoId == null
                && localId == null
                && igrejaVinculoId == null
                && liberarConflitoAgenda == null;
    }

    /**
     * Indica se a atualização afeta campos considerados sensíveis (agenda, cancelamento, projeto,
     * local ou igreja de vínculo).
     *
     * @return {@code true} se a requisição altera início, fim, motivo de cancelamento, status para
     *     CANCELADO, projeto, local ou igreja de vínculo
     */
    public boolean alteraCamposSensiveis() {
        return inicio != null || fim != null || canceladoMotivo != null || status == EventoStatusInput.CANCELADO ||
                projetoId != null || localId != null || igrejaVinculoId != null;
    }

    /**
     * Indica se o solicitante optou por ignorar a validação de conflito de agenda ao atualizar o
     * evento. Esta flag só tem efeito para atores com papel de Pároco.
     *
     * @return {@code true} se o conflito de agenda deve ser liberado na atualização
     */
    public boolean isLiberarConflitoAgenda() {
        return Boolean.TRUE.equals(liberarConflitoAgenda);
    }
}
