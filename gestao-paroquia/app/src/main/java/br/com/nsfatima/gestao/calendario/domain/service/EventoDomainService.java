package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.domain.policy.IntegridadeCalendarioPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Serviço de domínio que centraliza as invariantes estruturais de um evento de
 * calendário: consistência do intervalo temporal (delegada a
 * {@link IntegridadeCalendarioPolicy}), obrigatoriedade de justificativa para
 * eventos {@code ADICIONADO_EXTRA} e integridade da lista de participantes em
 * relação à organização responsável. Também expõe a tradução de sobreposição de
 * agenda detectada para o estado/motivo de conflito correspondentes.
 */
public class EventoDomainService {

    private final IntegridadeCalendarioPolicy calendarIntegrityPolicy;

    public EventoDomainService(IntegridadeCalendarioPolicy calendarIntegrityPolicy) {
        this.calendarIntegrityPolicy = calendarIntegrityPolicy;
    }

    /**
     * Valida as invariantes estruturais de um evento: o intervalo temporal precisa
     * ser consistente e, quando o status é {@code ADICIONADO_EXTRA} (evento criado
     * fora do calendário anual regular), uma justificativa não vazia é obrigatória.
     *
     * @param inicio instante de início do evento
     * @param fim instante de fim do evento
     * @param status status do evento; {@code null} é tratado como {@code RASCUNHO}
     * @param justificativa justificativa do evento, obrigatória apenas para status {@code ADICIONADO_EXTRA}
     * @throws IllegalArgumentException se o intervalo for inconsistente, ou se o status for {@code ADICIONADO_EXTRA}
     *     sem justificativa
     */
    public void validateEvento(Instant inicio, Instant fim, EventoStatusInput status, String justificativa) {
        calendarIntegrityPolicy.validateInterval(inicio, fim);
        EventoStatusInput canonicalStatus = status == null ? EventoStatusInput.RASCUNHO : status;
        if (canonicalStatus == EventoStatusInput.ADICIONADO_EXTRA
                && (justificativa == null || justificativa.isBlank())) {
            throw new IllegalArgumentException("ADICIONADO_EXTRA exige justificativa");
        }
    }

    /**
     * Valida a consistência entre a organização responsável e a lista de
     * participantes de um evento: a organização responsável é obrigatória, não
     * pode se repetir na lista de participantes e a lista não pode conter
     * duplicatas.
     *
     * @param grupoParoquialResponsavelId identificador da organização responsável pelo evento
     * @param participantes lista de identificadores das organizações participantes, ou {@code null}/vazia
     * @throws IllegalArgumentException se a organização responsável estiver ausente, se ela se repetir entre os
     *     participantes, ou se a lista de participantes tiver duplicatas
     */
    public void validateGrupoParoquialParticipantes(UUID grupoParoquialResponsavelId, List<UUID> participantes) {
        if (grupoParoquialResponsavelId == null) {
            throw new IllegalArgumentException("grupoParoquialResponsavelId deve ser informado");
        }
        if (participantes == null || participantes.isEmpty()) {
            return;
        }

        if (participantes.contains(grupoParoquialResponsavelId)) {
            throw new IllegalArgumentException("grupoParoquial responsavel nao pode repetir em participantes");
        }

        if (new HashSet<>(participantes).size() != participantes.size()) {
            throw new IllegalArgumentException("participantes nao podem conter valores duplicados");
        }
    }

    /**
     * Traduz a existência (ou não) de sobreposição de agenda detectada previamente
     * para o estado de conflito correspondente.
     *
     * @param hasOverlap se foi detectada sobreposição com outro evento existente
     * @return estado de conflito correspondente; {@code null} se não houver sobreposição
     */
    public EstadoConflitoAgenda resolveEstadoConflito(boolean hasOverlap) {
        return calendarIntegrityPolicy.resolveEstadoConflito(hasOverlap);
    }

    /**
     * Traduz a existência (ou não) de sobreposição de agenda detectada previamente
     * para uma mensagem descritiva do motivo do conflito.
     *
     * @param hasOverlap se foi detectada sobreposição com outro evento existente
     * @return mensagem descrevendo o conflito; {@code null} se não houver sobreposição
     */
    public String resolveMotivoConflito(boolean hasOverlap) {
        return calendarIntegrityPolicy.resolveMotivoConflito(hasOverlap);
    }
}
