package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/** Proposta pendente exibida exclusivamente à Secretaria/Administração. */
public record SolicitacaoAjusteCadastralResponse(
        UUID id,
        UUID fielId,
        String status,
        String email,
        String celular,
        String telefoneFixo,
        EnderecoDto endereco,
        Instant criadaEmUtc) {
}
