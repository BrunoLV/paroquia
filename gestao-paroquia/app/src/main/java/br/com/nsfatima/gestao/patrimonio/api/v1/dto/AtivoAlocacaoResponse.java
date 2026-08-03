package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa uma alocação (empréstimo/reserva por período ou uso vinculado a um evento) de um
 * ativo patrimonial. {@code impactadaPorAtraso} sinaliza quando a devolução de uma alocação
 * anterior atrasou e afetou a disponibilidade desta; {@code devolvidoEmUtc} é nulo enquanto o
 * ativo ainda está em posse do destino.
 */
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
