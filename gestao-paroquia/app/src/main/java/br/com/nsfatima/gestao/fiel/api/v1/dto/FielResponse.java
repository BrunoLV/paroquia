package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;

import java.time.LocalDate;
import java.util.UUID;

public record FielResponse(
        UUID id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String telefone,
        String endereco,
        Vocacao vocacao,
        boolean ativo,
        boolean batizado,
        boolean primeiraComunhao,
        boolean crismado,
        boolean casadoIgrejaCatolica
) {
}
