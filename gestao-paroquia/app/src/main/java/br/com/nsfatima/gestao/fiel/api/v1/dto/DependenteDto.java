package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.Parentesco;

import java.time.LocalDate;
import java.util.UUID;

public record DependenteDto(
        UUID id,
        String nome,
        LocalDate dataNascimento,
        Parentesco parentesco,
        Boolean batizado,
        Boolean primeiraComunhao,
        Boolean crismado
) {
}
