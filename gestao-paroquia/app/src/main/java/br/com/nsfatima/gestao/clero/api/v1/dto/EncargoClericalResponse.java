package br.com.nsfatima.gestao.clero.api.v1.dto;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import java.time.Instant;
import java.util.UUID;

/** Representação de provisão ou encargo canônico paroquial. */
public record EncargoClericalResponse(
        UUID id,
        UUID clerigoId,
        UUID unidadeTerritorialId,
        FuncaoEclesiastica funcao,
        boolean atual,
        Instant criadoEmUtc,
        Instant encerradoEmUtc) {
}
