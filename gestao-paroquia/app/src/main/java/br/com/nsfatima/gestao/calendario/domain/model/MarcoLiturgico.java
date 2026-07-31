package br.com.nsfatima.gestao.calendario.domain.model;

import java.time.LocalDate;

public record MarcoLiturgico(
        String chave,
        String nome,
        LocalDate data,
        CorLiturgica corLiturgica,
        Double fatorDemanda
) {
}
