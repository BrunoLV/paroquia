package br.com.nsfatima.gestao.organizacao.domain.policy;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Regra de duração do mandato de coordenação: a coordenação é um encargo eclesial temporário, nunca vitalício.
 * O limite de três anos é uma diretriz de gestão da paróquia (as normas diocesanas costumam variar entre dois e
 * três anos) e pode ser renovado por um novo mandato, sempre homologado pelo Pároco.
 */
public final class PoliticaDuracaoMandato {

    /** Duração máxima, em anos, de um mandato de coordenação. */
    public static final int DURACAO_MAXIMA_ANOS = 3;

    private PoliticaDuracaoMandato() {
    }

    /**
     * Avalia o período proposto.
     *
     * @param inicio início do mandato
     * @param fim término do mandato
     * @return mensagem de violação, ou vazio quando o período é válido
     */
    public static Optional<String> violacao(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            return Optional.of("Início e término do mandato são obrigatórios.");
        }
        if (!fim.isAfter(inicio)) {
            return Optional.of("O término do mandato deve ser posterior ao início.");
        }
        if (fim.isAfter(inicio.plusYears(DURACAO_MAXIMA_ANOS))) {
            return Optional.of("O mandato não pode exceder " + DURACAO_MAXIMA_ANOS + " anos.");
        }
        return Optional.empty();
    }
}
