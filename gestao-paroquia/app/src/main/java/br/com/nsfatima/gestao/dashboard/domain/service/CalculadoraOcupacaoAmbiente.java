package br.com.nsfatima.gestao.dashboard.domain.service;

import br.com.nsfatima.gestao.dashboard.domain.model.JanelaTemporal;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Calcula quantos minutos um ambiente ficou ocupado dentro de uma janela, a partir das reservas do
 * local (a fonte de verdade da ocupação). Cada reserva é recortada na janela e as reservas que se
 * sobrepõem são unidas antes da soma: dois grupos reservando a mesma sala no mesmo horário, ou uma
 * reserva de evento somada a outra manual, ocupam a sala uma vez só, senão a taxa passaria de 100%
 * artificialmente.
 */
public class CalculadoraOcupacaoAmbiente {

    /**
     * Soma a ocupação efetiva, sem dupla contagem de sobreposições.
     *
     * @param reservas intervalos reservados do mesmo ambiente (podem exceder a janela e se sobrepor)
     * @param janela recorte em que a ocupação é medida
     * @return minutos distintos ocupados dentro da janela
     */
    public long minutosOcupados(List<JanelaTemporal> reservas, JanelaTemporal janela) {
        List<JanelaTemporal> ordenadas = reservas.stream()
                .filter(r -> r.inicio().isBefore(janela.fim()) && r.fim().isAfter(janela.inicio()))
                .map(r -> new JanelaTemporal(max(r.inicio(), janela.inicio()), min(r.fim(), janela.fim())))
                .sorted(Comparator.comparing(JanelaTemporal::inicio))
                .toList();
        Duration total = Duration.ZERO;
        Instant inicioAtual = null;
        Instant fimAtual = null;
        for (JanelaTemporal r : ordenadas) {
            if (fimAtual == null || r.inicio().isAfter(fimAtual)) {
                total = total.plus(duracao(inicioAtual, fimAtual));
                inicioAtual = r.inicio();
                fimAtual = r.fim();
            } else if (r.fim().isAfter(fimAtual)) {
                fimAtual = r.fim();
            }
        }
        return total.plus(duracao(inicioAtual, fimAtual)).toMinutes();
    }

    private static Duration duracao(Instant inicio, Instant fim) {
        return inicio == null ? Duration.ZERO : Duration.between(inicio, fim);
    }

    private static Instant max(Instant a, Instant b) {
        return a.isAfter(b) ? a : b;
    }

    private static Instant min(Instant a, Instant b) {
        return a.isBefore(b) ? a : b;
    }
}
