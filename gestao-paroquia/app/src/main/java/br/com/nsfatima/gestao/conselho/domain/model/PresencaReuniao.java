package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Registro de presença da reunião: ou uma lista nominal, ou apenas o total de participantes (assembleias).
 * Os dois modos são excludentes; qual deles vale para a reunião é decidido pelo tipo
 * ({@link ModoPresenca}) e conferido no agregado. O estado vazio existe para o rascunho ainda não preenchido.
 */
public record PresencaReuniao(List<Presenca> nominal, Integer total) {

    public PresencaReuniao {
        nominal = nominal == null ? List.of() : List.copyOf(nominal);
        if (total != null && total < 0) {
            throw new AtaInvalidaException("O total de participantes não pode ser negativo.");
        }
        if (total != null && !nominal.isEmpty()) {
            throw new AtaInvalidaException("A presença é nominal ou quantitativa, nunca as duas.");
        }
        Set<UUID> vistos = new HashSet<>();
        for (Presenca presenca : nominal) {
            if (!vistos.add(presenca.fielId())) {
                throw new AtaInvalidaException("O mesmo fiel não pode constar duas vezes na presença.");
            }
        }
    }

    public static PresencaReuniao vazia() {
        return new PresencaReuniao(List.of(), null);
    }

    public static PresencaReuniao nominal(List<Presenca> presencas) {
        return new PresencaReuniao(presencas, null);
    }

    public static PresencaReuniao quantitativa(int total) {
        return new PresencaReuniao(List.of(), total);
    }

    /** Indica se algum dado de presença foi informado. */
    public boolean informada() {
        return !nominal.isEmpty() || total != null;
    }

    /** Indica se o registro é uma lista de pessoas. */
    public boolean ehNominal() {
        return !nominal.isEmpty();
    }

    /** Quantidade de presentes, seja pela lista nominal ou pelo total informado. */
    public int presentes() {
        if (total != null) {
            return total;
        }
        return (int) nominal.stream().filter(Presenca::presente).count();
    }

    /** Identificadores dos fiéis que estiveram presentes (vazio na presença quantitativa). */
    public Set<UUID> fieisPresentes() {
        Set<UUID> ids = new HashSet<>();
        for (Presenca presenca : nominal) {
            if (presenca.presente()) {
                ids.add(presenca.fielId());
            }
        }
        return Set.copyOf(ids);
    }
}
