package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.validation.ValidaDatasEvento;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
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
 * organização responsável.
 */
@ValidaDatasEvento
@Schema(description = "Solicitação para criação de um novo evento")
public record CriarEventoRequest(
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

        @Schema(description = "ID do local onde o evento ocorrerá (legado/compatibilidade)")
        UUID localId,

        @Schema(description = "Lista de IDs das localidades onde o evento ocorrerá")
        List<UUID> localIds,

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

        @Schema(description = "Flag para liberar conflito de agenda (apenas Pároco)")
        Boolean liberarConflitoAgenda,

        @Schema(description = "Lista de IDs dos ativos patrimoniais alocados para o evento")
        List<UUID> ativoIds,

        @Schema(description = "Regra de recorrência caso o evento seja repetitivo")
        RegraRecorrencia regraRecorrencia,

        @Schema(description = "Indica que o usuário confirmou envio para discussão com conselho mesmo com conflitos")
        Boolean confirmarComConflito,

        @Schema(description = "Formato de horário do evento (PERSONALIZADO, DIA_INTEIRO, MADRUGADA, MANHA, TARDE, NOITE)")
        TipoHorarioEvento tipoHorario,

        @Schema(description = "Formato de realização do evento (PRESENCIAL, ONLINE, HIBRIDO)")
        FormatoEvento formato,

        @Schema(description = "Lista de períodos do dia selecionados quando o horário for por turnos (ex: MANHA, NOITE)")
        List<TipoHorarioEvento> periodos) {

    public CriarEventoRequest {
        if (tipoHorario == null) {
            tipoHorario = TipoHorarioEvento.PERSONALIZADO;
        }
        if (localIds == null || localIds.isEmpty()) {
            localIds = localId != null ? List.of(localId) : List.of();
        } else if (localId == null) {
            localId = localIds.getFirst();
        }
        if (formato == null) {
            formato = !localIds.isEmpty() ? FormatoEvento.PRESENCIAL : FormatoEvento.ONLINE;
        }
        if (periodos == null) {
            periodos = List.of();
        }
    }

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            UUID localId,
            List<UUID> localIds,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes,
            Boolean liberarConflitoAgenda,
            List<UUID> ativoIds,
            RegraRecorrencia regraRecorrencia,
            Boolean confirmarComConflito,
            TipoHorarioEvento tipoHorario,
            FormatoEvento formato) {
        this(titulo, descricao, categoria, organizacaoResponsavelId, projetoId, localId,
                localIds, igrejaVinculoId, inicio, fim, status, adicionadoExtraJustificativa,
                participantes, liberarConflitoAgenda, ativoIds, regraRecorrencia, confirmarComConflito,
                tipoHorario, formato, List.of());
    }

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            UUID localId,
            List<UUID> localIds,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes,
            Boolean liberarConflitoAgenda,
            List<UUID> ativoIds,
            RegraRecorrencia regraRecorrencia,
            Boolean confirmarComConflito,
            TipoHorarioEvento tipoHorario) {
        this(titulo, descricao, categoria, organizacaoResponsavelId, projetoId, localId,
                localIds, igrejaVinculoId,
                inicio, fim, status, adicionadoExtraJustificativa, participantes, liberarConflitoAgenda, ativoIds,
                regraRecorrencia, confirmarComConflito, tipoHorario, null);
    }

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes,
            Boolean liberarConflitoAgenda,
            List<UUID> ativoIds,
            RegraRecorrencia regraRecorrencia,
            Boolean confirmarComConflito) {
        this(titulo, descricao, categoria, organizacaoResponsavelId, projetoId, localId,
                localId != null ? List.of(localId) : List.of(), igrejaVinculoId,
                inicio, fim, status, adicionadoExtraJustificativa, participantes, liberarConflitoAgenda, ativoIds,
                regraRecorrencia, confirmarComConflito, TipoHorarioEvento.PERSONALIZADO);
    }

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes,
            Boolean liberarConflitoAgenda,
            List<UUID> ativoIds) {
        this(titulo, descricao, categoria, organizacaoResponsavelId, projetoId, localId,
                localId != null ? List.of(localId) : List.of(), igrejaVinculoId,
                inicio, fim, status, adicionadoExtraJustificativa, participantes, liberarConflitoAgenda, ativoIds,
                null, false, TipoHorarioEvento.PERSONALIZADO);
    }

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes,
            Boolean liberarConflitoAgenda) {
        this(titulo, descricao, categoria, organizacaoResponsavelId, projetoId, localId, igrejaVinculoId,
                inicio, fim, status, adicionadoExtraJustificativa, participantes, liberarConflitoAgenda,
                List.of(), null, false);
    }

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            UUID localId,
            UUID igrejaVinculoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes) {
        this(titulo, descricao, categoria, organizacaoResponsavelId, projetoId, localId,
                igrejaVinculoId, inicio, fim, status, adicionadoExtraJustificativa, participantes, false);
    }

    public CriarEventoRequest(
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            UUID localId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            List<UUID> participantes) {
        this(titulo,
                descricao, categoria, organizacaoResponsavelId, projetoId, localId, null, inicio, fim, status,
                adicionadoExtraJustificativa, participantes, false);
    }

    /**
     * Alias semântico de {@code igrejaVinculoId} usado pelo domínio para identificar a comunidade
     * (Matriz ou Comunidade) à qual o evento está vinculado.
     *
     * @return o ID da igreja de vínculo do evento
     */
    public UUID comunidadeId() {
        return igrejaVinculoId;
    }

    /**
     * Indica se o solicitante optou por ignorar a validação de conflito de agenda ao criar o evento.
     * Esta flag só tem efeito para atores com papel de Pároco.
     *
     * @return {@code true} se o conflito de agenda deve ser liberado na criação
     */
    public boolean isLiberarConflitoAgenda() {
        return Boolean.TRUE.equals(liberarConflitoAgenda);
    }

    /**
     * Indica se o usuário optou por enviar para deliberação do conselho mesmo com conflitos apontados.
     *
     * @return {@code true} se o usuário confirmou envio para discussão com o conselho
     */
    public boolean isConfirmarComConflito() {
        return Boolean.TRUE.equals(confirmarComConflito);
    }
}
