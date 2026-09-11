package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Representação completa de um evento retornada pela API, incluindo o estado de conflito de agenda
 * e o vínculo com uma série recorrente, quando aplicável.
 *
 * @param status status atual do evento; quando o evento tem uma alteração aguardando aprovação, o
 *     status refletido aqui é o efetivo, não o pendente — a existência de uma solicitação pendente
 *     é consultada separadamente (ver {@link EventoAprovacaoPendenteResponse})
 * @param recorrenciaId identificador da série de recorrência à qual este evento pertence, ou
 *     {@code null} se o evento não foi gerado a partir de uma regra de recorrência
 * @param estadoConflito nome de {@code EstadoConflitoAgenda} ("PENDENTE") quando a
 *     {@code IntegridadeCalendarioPolicy} detecta sobreposição de agenda no mesmo espaço/período
 *     para este evento, ou {@code null} quando não há conflito detectado; a resolução do conflito
 *     (aprovação/rejeição) é tratada pelo fluxo de aprovação, não por este campo
 * @param motivoConflito descrição textual do conflito de agenda detectado, preenchida apenas quando
 *     {@code estadoConflito} não é {@code null}
 */
public record EventoResponse(
        UUID id,
        String titulo,
        String descricao,
        CategoriaEvento categoria,
        UUID grupoParoquialResponsavelId,
        UUID projetoId,
        String nomeProjeto,
        Instant inicio,
        Instant fim,
        EventoStatusResponse status,
        UUID localId,
        UUID igrejaVinculoId,
        List<UUID> participantes,
        UUID recorrenciaId,
        String estadoConflito,
        String motivoConflito,
        String adicionadoExtraJustificativa,
        Integer nivelPrioridade,
        Boolean autorizadoPeloCpp,
        SetorPastoral setorPastoral) {
}
