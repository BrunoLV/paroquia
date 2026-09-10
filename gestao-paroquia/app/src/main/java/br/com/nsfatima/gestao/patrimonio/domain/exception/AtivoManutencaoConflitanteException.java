package br.com.nsfatima.gestao.patrimonio.domain.exception;

import br.com.nsfatima.gestao.patrimonio.domain.model.ReservaImpeditiva;

import java.util.List;

/**
 * Lançada quando o envio de um ativo para manutenção é bloqueado por existirem reservas
 * {@code CONFIRMADA} ou {@code PENDENTE} no intervalo da manutenção. Carrega a lista das reservas
 * impeditivas para que a interface possa exibi-las ao operador, que deve cancelá-las previamente.
 */
public class AtivoManutencaoConflitanteException extends RuntimeException {

    private final transient List<ReservaImpeditiva> reservasImpeditivas;

    public AtivoManutencaoConflitanteException(List<ReservaImpeditiva> reservasImpeditivas) {
        super("Existem %d reserva(s) no período da manutenção que precisam ser canceladas antes."
                .formatted(reservasImpeditivas.size()));
        this.reservasImpeditivas = List.copyOf(reservasImpeditivas);
    }

    public List<ReservaImpeditiva> getReservasImpeditivas() {
        return reservasImpeditivas;
    }
}
