package br.com.nsfatima.gestao.calendario.domain.model;

import java.time.LocalDate;
import java.util.Map;

public record SaidaLiturgica(
        int anoNominal,
        LocalDate dataInicio,
        LocalDate dataFim,
        String cicloLetra,
        Map<String, MarcoLiturgico> marcosCalculados
) {
}
