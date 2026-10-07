package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representação pública de um ativo patrimonial. Todos os campos exceto {@code descricao} são
 * imutáveis após o cadastro; {@code isReservavel} define elegibilidade a reserva no módulo de
 * Reservas.
 */
public record AtivoPatrimonialResponse(
        UUID id,
        String nome,
        String codigoTombamento,
        String descricao,
        AtivoCategoria categoria,
        AtivoStatus status,
        boolean isReservavel,
        UUID organizacaoId,
        LocalDate dataAquisicao,
        BigDecimal valorAquisicao,
        UUID cadastradoPorId,
        Instant criadoEmUtc,
        Instant atualizadoEmUtc
) {

    public static AtivoPatrimonialResponse de(AtivoPatrimonial ativo) {
        return new AtivoPatrimonialResponse(
                ativo.getId().value(),
                ativo.getNome(),
                ativo.getCodigoTombamento().valor(),
                ativo.getDescricao(),
                ativo.getCategoria(),
                ativo.getStatus(),
                ativo.isReservavel(),
                ativo.getOrganizacaoId(),
                ativo.getDataAquisicao(),
                ativo.getValorAquisicao(),
                ativo.getCadastradoPorId(),
                ativo.getCreatedAt(),
                ativo.getUpdatedAt());
    }
}
