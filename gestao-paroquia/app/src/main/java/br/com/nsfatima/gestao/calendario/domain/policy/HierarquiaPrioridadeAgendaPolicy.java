package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Aplica a precedência 1–4 da agenda eclesial sobre fatos já carregados em memória. */
public final class HierarquiaPrioridadeAgendaPolicy implements PrioridadeAgendaPolicy {

    @Override
    public List<EventoSobreposto> resolver(EventoSobreposto novoEvento, List<EventoSobreposto> existentes) {
        List<EventoSobreposto> cancelamentos = new ArrayList<>();
        for (EventoSobreposto existente : existentes) {
            boolean mesmoEspaco = compartilhaIdentificador(novoEvento.localId(), existente.localId())
                    || compartilhaIdentificador(novoEvento.igrejaId(), existente.igrejaId());
            if (novoEvento.prioridade() == 1 && existente.prioridade() > 1) {
                cancelamentos.add(existente);
            } else if (novoEvento.prioridade() == 1 && existente.prioridade() == 1) {
                throw new ConflitoAgendaEclesialException("Já existe Solenidade Global para o período");
            } else if (novoEvento.prioridade() == 2 && existente.prioridade() == 1) {
                throw new ConflitoAgendaEclesialException("Nível 2 não pode sobrepor Nível 1");
            } else if (novoEvento.prioridade() == 2 && mesmoEspaco && existente.prioridade() == 2) {
                throw new ConflitoAgendaEclesialException("Já existe evento litúrgico no mesmo espaço");
            } else if (novoEvento.prioridade() == 2 && mesmoEspaco && existente.prioridade() > 2) {
                cancelamentos.add(existente);
            } else if (novoEvento.prioridade() >= 3 && (existente.prioridade() < novoEvento.prioridade() || mesmoEspaco)) {
                throw new ConflitoAgendaEclesialException("Conflito de prioridade ou espaço na agenda eclesial");
            }
        }
        return List.copyOf(cancelamentos);
    }

    private boolean compartilhaIdentificador(UUID primeiro, UUID segundo) {
        return primeiro != null && Objects.equals(primeiro, segundo);
    }
}
