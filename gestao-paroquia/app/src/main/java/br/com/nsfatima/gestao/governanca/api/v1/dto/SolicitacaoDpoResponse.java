package br.com.nsfatima.gestao.governanca.api.v1.dto;

import br.com.nsfatima.gestao.governanca.domain.model.StatusSolicitacaoDpo;

import java.time.Instant;
import java.util.UUID;

/** Resposta de consulta de solicitação de titular ao DPO. */
public record SolicitacaoDpoResponse(
        UUID id,
        String protocolo,
        String titularNome,
        String titularEmail,
        String titularCpf,
        String tipoSolicitacao,
        String mensagem,
        StatusSolicitacaoDpo status,
        String resposta,
        Instant prazoRespostaUtc,
        Instant respondidoEmUtc,
        Instant criadoEmUtc
) {}
