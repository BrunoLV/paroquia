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
 * verificado por {@link #changesSensitiveFields()}. Os dois construtores compactos legados existem
 * para compatibilidade com chamadores anteriores à introdução de {@code igrejaVinculoId} e
 * {@code bypassAgendaConflict}.
 */
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
        UUID grupoParoquialResponsavelId,

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
        Boolean bypassAgendaConflict,

        @Schema(description = "Lista de IDs dos ativos patrimoniais alocados para o evento")
        List<UUID> ativoIds) {

    public UpdateEventoRequest(
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
            EventoEditScope editScope,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Boolean bypassAgendaConflict) {
        this(titulo, descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                grupoParoquialResponsavelId, participantes, editScope, projetoId, localId, igrejaVinculoId, bypassAgendaConflict, null);
    }

    /**
     * Construtor legado sem {@code igrejaVinculoId} e sem {@code bypassAgendaConflict}, mantido para
     * compatibilidade com chamadores anteriores à introdução desses campos. Assume que o vínculo de
     * igreja não é alterado e que o conflito de agenda não deve ser ignorado.
     */
    public UpdateEventoRequest(
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
            EventoEditScope editScope,
            UUID projetoId,
            UUID localId) {
        this(titulo,
                descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                grupoParoquialResponsavelId, participantes, editScope, projetoId, localId, null, false);

    }

    /**
     * Construtor legado sem {@code bypassAgendaConflict}, mantido para compatibilidade com
     * chamadores anteriores à introdução da flag de bypass. Assume que o conflito de agenda não
     * deve ser ignorado.
     */
    public UpdateEventoRequest(
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
            EventoEditScope editScope,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId) {
        this(titulo,
                descricao, categoria, inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo,
                grupoParoquialResponsavelId, participantes, editScope, projetoId, localId, igrejaVinculoId, false);

    }

    /**
     * Indica se nenhum campo de atualização foi informado, ou seja, se a requisição não solicita
     * nenhuma alteração efetiva sobre o evento. Usado para rejeitar/ignorar requisições vazias antes
     * de acionar o fluxo de atualização (ou aprovação).
     *
     * @return {@code true} se todos os campos de atualização forem {@code null}
     */
    public boolean isEmptyPayload() {

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
                && bypassAgendaConflict == null;
    }

    /**
     * Indica se a atualização afeta campos considerados sensíveis (agenda, cancelamento, projeto,
     * local ou igreja de vínculo), usado para decidir se a alteração deve ser roteada pelo fluxo de
     * aprovação em vez de aplicada diretamente.
     *
     * @return {@code true} se a requisição altera início, fim, motivo de cancelamento, status para
     *     CANCELADO, projeto, local ou igreja de vínculo
     */
    public boolean changesSensitiveFields() {
        return inicio != null || fim != null || canceladoMotivo != null || status == EventoStatusInput.CANCELADO ||
                projetoId != null || localId != null || igrejaVinculoId != null;
    }

    /**
     * Indica se o solicitante optou por ignorar a validação de conflito de agenda ao atualizar o
     * evento. Esta flag só tem efeito para atores com papel de Pároco; para os demais é ignorada
     * pela camada de domínio.
     *
     * @return {@code true} se o conflito de agenda deve ser ignorado na atualização
     */
    public boolean isBypassAgendaConflict() {
        return Boolean.TRUE.equals(bypassAgendaConflict);
    }
}
