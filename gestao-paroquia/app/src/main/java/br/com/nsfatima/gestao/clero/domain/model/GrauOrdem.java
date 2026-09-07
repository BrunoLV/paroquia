package br.com.nsfatima.gestao.clero.domain.model;

import java.time.LocalDate;

/** Grau recebido pelo ministro ordenado e suas ordenações indispensáveis. */
public enum GrauOrdem {
    DIACONO_TRANSITORIO,
    DIACONO_PERMANENTE,
    PADRE_DIOCESANO,
    PADRE_RELIGIOSO,
    BISPO;

    public void validarOrdenacoes(LocalDate diaconal, LocalDate presbiteral, LocalDate episcopal) {
        boolean diaconoObrigatorio = diaconal == null;
        boolean presbiteroObrigatorio = (this == PADRE_DIOCESANO || this == PADRE_RELIGIOSO || this == BISPO)
                && presbiteral == null;
        boolean bispoObrigatorio = this == BISPO && episcopal == null;
        if (diaconoObrigatorio || presbiteroObrigatorio || bispoObrigatorio) {
            throw new IllegalArgumentException(
                    "As datas de ordenação exigidas para o grau da ordem devem ser informadas.");
        }
        if ((presbiteral != null && presbiteral.isBefore(diaconal))
                || (episcopal != null && (presbiteral == null || episcopal.isBefore(presbiteral)))) {
            throw new IllegalArgumentException("As datas de ordenação devem respeitar a sequência canônica.");
        }
    }
}
