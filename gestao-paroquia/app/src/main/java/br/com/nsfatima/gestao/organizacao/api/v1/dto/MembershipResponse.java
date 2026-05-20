package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.UUID;

public record MembershipResponse(
    UUID id,
    UUID organizacaoId,
    String tipo,
    String papel,
    boolean ativo
) {}
