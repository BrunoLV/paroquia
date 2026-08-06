package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;

import java.time.LocalDate;

public record UpdateFielRequest(
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String endereco,
        Vocacao vocacao,
        Boolean ativo,
        Boolean batizado,
        Boolean primeiraComunhao,
        Boolean crismado,
        Boolean casadoIgrejaCatolica
) {
}
