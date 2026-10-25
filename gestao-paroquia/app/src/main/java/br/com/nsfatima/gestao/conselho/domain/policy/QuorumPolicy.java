package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.conselho.domain.model.ResultadoQuorum;

/**
 * Quórum de colegiado: maioria absoluta dos membros convocados (metade mais um). Stateless e zero-I/O.
 * Na reunião conjunta do CPP e do CAEP, os membros convocados são a soma dos dois conselhos.
 */
public final class QuorumPolicy {

    private QuorumPolicy() {
    }

    /**
     * Avalia o quórum.
     *
     * @param membrosConvocados total de membros ativos convocados, congelado na submissão da ata
     * @param presentes quantidade de membros presentes
     * @return o resultado, com o mínimo necessário e se foi atingido
     */
    public static ResultadoQuorum avaliar(int membrosConvocados, int presentes) {
        int minimo = membrosConvocados / 2 + 1;
        return new ResultadoQuorum(membrosConvocados, minimo, presentes, presentes >= minimo);
    }
}
