package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;

import java.time.Instant;

/**
 * Política de integridade do intervalo de tempo de um evento de calendário —
 * garante que todo evento tenha um intervalo temporal válido (início antes do fim,
 * ambos informados) e traduz a detecção de sobreposição de agenda (calculada fora
 * desta classe) para o estado/motivo de conflito expostos ao restante do domínio.
 */
public class IntegridadeCalendarioPolicy {

    /**
     * Valida que o intervalo de tempo do evento é consistente: início e fim devem
     * estar preenchidos e o fim deve ser estritamente posterior ao início.
     *
     * @param inicioUtc instante de início do evento, em UTC
     * @param fimUtc instante de fim do evento, em UTC
     * @throws IllegalArgumentException se início ou fim estiverem ausentes, ou se o fim não for posterior ao início
     */
    public void validateInterval(Instant inicioUtc, Instant fimUtc) {
        if (inicioUtc == null || fimUtc == null) {
            throw new IllegalArgumentException("inicio/fim devem ser informados");
        }
        if (!fimUtc.isAfter(inicioUtc)) {
            throw new IllegalArgumentException("fim deve ser maior que inicio");
        }
    }

    /**
     * Traduz a existência (ou não) de sobreposição de agenda detectada previamente
     * para o estado de conflito correspondente.
     *
     * @param hasOverlap se foi detectada sobreposição com outro evento existente
     * @return {@link EstadoConflitoAgenda#PENDENTE} se houver sobreposição, ou {@code null} caso contrário
     */
    public EstadoConflitoAgenda resolveEstadoConflito(boolean hasOverlap) {
        return hasOverlap ? EstadoConflitoAgenda.PENDENTE : null;
    }

    /**
     * Traduz a existência (ou não) de sobreposição de agenda detectada previamente
     * para uma mensagem descritiva do motivo do conflito.
     *
     * @param hasOverlap se foi detectada sobreposição com outro evento existente
     * @return mensagem descrevendo o conflito, ou {@code null} se não houver sobreposição
     */
    public String resolveMotivoConflito(boolean hasOverlap) {
        return hasOverlap ? "Sobreposição detectada com um evento existente" : null;
    }
}
