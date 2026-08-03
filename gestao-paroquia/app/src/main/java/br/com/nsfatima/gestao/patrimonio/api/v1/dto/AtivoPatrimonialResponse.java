package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representação pública de um ativo patrimonial da paróquia. {@code reservavel} indica se o
 * ativo pode ser alocado/reservado para eventos (alguns ativos existem apenas para controle
 * patrimonial e nunca são alocáveis).
 */
public record AtivoPatrimonialResponse(
        UUID id,
        String nome,
        String numeroTombamento,
        AtivoCategoria categoria,
        AtivoStatus status,
        boolean reservavel,
        UUID organizacaoId,
        LocalDate dataAquisicao,
        BigDecimal valorEstimado,
        Instant criadoEmUtc
) {
}
