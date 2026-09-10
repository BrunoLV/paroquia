package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada pela {@code PrioridadeAgendaPolicy} quando um novo evento
 * sobrepõe (mesmo espaço/comunidade e período) outro evento de prioridade igual ou
 * maior na hierarquia eclesial — solenidade global (Nível 1), evento litúrgico
 * (Nível 2), atividade paroquial (Nível 3) ou de movimento (Nível 4). A regra
 * garante que a agenda eclesial não permita conflitos entre eventos de níveis que
 * não podem coexistir.
 */
public class ConflitoAgendaEclesialException extends RuntimeException {
    /**
     * Cria a exceção com a mensagem padrão de conflito de agenda eclesial.
     */
    public ConflitoAgendaEclesialException() {
        super("Conflito de Agenda Eclesial");
    }

    /**
     * Cria a exceção com a mensagem descrevendo o conflito de agenda específico.
     *
     * @param message descrição do conflito de prioridade/sobreposição detectado
     */
    public ConflitoAgendaEclesialException(String message) {
        super(message);
    }
}
