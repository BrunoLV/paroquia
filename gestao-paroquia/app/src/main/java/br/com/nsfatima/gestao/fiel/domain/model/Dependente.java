package br.com.nsfatima.gestao.fiel.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade de valor imutável do Dependente / Filho do Fiel para acompanhamento da Pastoral Familiar e Catequese.
 */
public record Dependente(
        UUID id,
        String nome,
        LocalDate dataNascimento,
        Parentesco parentesco,
        boolean batizado,
        boolean primeiraComunhao,
        boolean crismado
) {
}
