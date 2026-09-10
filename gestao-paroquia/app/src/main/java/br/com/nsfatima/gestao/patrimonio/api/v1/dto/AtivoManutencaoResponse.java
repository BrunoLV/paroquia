package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;

import java.time.Instant;
import java.util.UUID;

/** Representação pública de um registro de manutenção de ativo. */
public record AtivoManutencaoResponse(
        UUID id,
        UUID ativoId,
        String descricaoReparo,
        Instant dataInicio,
        Instant dataFimPrevista,
        Instant dataFimReal,
        UUID registradoPorId,
        Instant criadoEmUtc
) {

    public static AtivoManutencaoResponse de(AtivoManutencao m) {
        return new AtivoManutencaoResponse(
                m.getId().value(),
                m.getAtivoId().value(),
                m.getDescricaoReparo(),
                m.getPeriodo().inicio(),
                m.getPeriodo().fimPrevista(),
                m.getDataFimReal(),
                m.getRegistradoPorId(),
                m.getCreatedAt());
    }
}
