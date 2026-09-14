package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.validation.ValidEventoDates;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Solicitação para criação de um novo evento no calendário. A criação pode ser roteada pelo fluxo
 * de aprovação em vez de ser aplicada diretamente, dependendo do papel do solicitante e da
 * organização responsável. Os dois construtores compactos legados existem para compatibilidade com
 * chamadores anteriores à introdução de {@code igrejaVinculoId} e {@code bypassAgendaConflict}.
 */
@ValidEventoDates
@Schema(description = "Solicitação para criação de um novo evento")
public record CriarEventoRequest(
        @Schema(description = "Título do evento", example = "Missa de Domingo")
        @NotBlank @Size(max = 160) String titulo,

        @Schema(description = "Descrição detalhada do evento", example = "Celebração dominical na paróquia")
        @Size(max = 4000) String descricao,

        @Schema(description = "Categoria do evento")
        CategoriaEvento categoria,

        @Schema(description = "ID da organização responsável pelo evento")
        @NotNull UUID grupoParoquialResponsavelId,

        @Schema(description = "ID do projeto ao qual o evento pertence")
        UUID projetoId,

        @Schema(description = "ID do local onde o evento ocorrerá")
        UUID localId,

        @Schema(description = "ID da igreja de vínculo (Matriz ou Comunidade)")
        UUID igrejaVinculoId,

        @Schema(description = "Data e hora de início do evento (UTC)")
        @NotNull Instant inicio,

        @Schema(description = "Data e hora de término do evento (UTC)")
        @NotNull Instant fim,

        @Schema(description = "Status inicial do evento")
        EventoStatusInput status,

        @Schema(description = "Justificativa caso o evento seja adicionado extra-ordinariamente")
        String adicionadoExtraJustificativa,

        @Schema(description = "Lista de IDs das organizações participantes")
        List<UUID> participantes,

        @Schema(description = "Flag de bypass para liberar conflito de agenda (apenas Pároco)")
        Boolean bypassAgendaConflict,

        @Schema(description = "Lista de IDs dos ativos patrimoniais alocados para o evento")
        List<UUID> ativoIds) {

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID grupoParoquialResponsavelId,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes,
            Boolean bypassAgendaConflict) {
        this(titulo, descricao, categoria, grupoParoquialResponsavelId, projetoId, localId, igrejaVinculoId,
                inicio, fim, status, adicionadoExtraJustificativa, participantes, bypassAgendaConflict, List.of());
    }

    /**
     * Construtor legado sem {@code igrejaVinculoId} e sem {@code bypassAgendaConflict}, mantido para
     * compatibilidade com chamadores anteriores à introdução desses campos. Assume que o evento não
     * possui vínculo de igreja específico e que o conflito de agenda não deve ser ignorado.
     */
    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID grupoParoquialResponsavelId,
            UUID projetoId,
            UUID localId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes) {
        this(titulo,
                descricao, categoria, grupoParoquialResponsavelId, projetoId, localId, null, inicio, fim, status,
                adicionadoExtraJustificativa, participantes, false);

    }

    /**
     * Construtor legado sem {@code bypassAgendaConflict}, mantido para compatibilidade com chamadores
     * anteriores à introdução da flag de bypass. Assume que o conflito de agenda não deve ser ignorado.
     */
    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID grupoParoquialResponsavelId,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes) {
        this(titulo,
                descricao, categoria, grupoParoquialResponsavelId, projetoId, localId,
                igrejaVinculoId, inicio, fim, status, adicionadoExtraJustificativa, participantes, false);

    }

    /**
     * Alias semântico de {@code igrejaVinculoId} usado pelo domínio para identificar a comunidade
     * (Matriz ou Comunidade) à qual o evento está vinculado.
     *
     * @return o ID da igreja de vínculo do evento
     */
    public UUID communityId() {
        return igrejaVinculoId;
    }

    /**
     * Indica se o solicitante optou por ignorar a validação de conflito de agenda ao criar o evento.
     * Esta flag só tem efeito para atores com papel de Pároco; para os demais é ignorada pela camada
     * de domínio.
     *
     * @return {@code true} se o conflito de agenda deve ser ignorado na criação
     */
    public boolean isBypassAgendaConflict() {
        return Boolean.TRUE.equals(bypassAgendaConflict);
    }
}
