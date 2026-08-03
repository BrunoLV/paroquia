package br.com.nsfatima.gestao.calendario.domain.model;

/**
 * Estado de conflito de agenda calculado por {@code IntegridadeCalendarioPolicy} ao
 * detectar sobreposição de eventos no mesmo espaço/período. Hoje representa apenas
 * a detecção da sobreposição em si ({@code PENDENTE}); a resolução do conflito
 * (aprovação, rejeição) é tratada pelo fluxo de aprovação em outro contexto.
 */
public enum EstadoConflitoAgenda {
    /** Sobreposição de agenda detectada e ainda não resolvida. */
    PENDENTE
}
