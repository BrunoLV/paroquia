package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AtivoAlocacaoResponse(
        UUID id,
        UUID ativoId,
        String ativoNome,
        String numeroTombamento,
        AtivoAlocacaoTipo tipo,
        UUID eventoId,
        UUID organizacaoOrigemId,
        UUID organizacaoDestinoId,
        boolean emprestimo,
        LocalDate dataInicio,
        LocalDate dataFim,
        AtivoAlocacaoStatus status,
        boolean impactadaPorAtraso,
        Instant criadoEmUtc,
        Instant devolvidoEmUtc) {
}
