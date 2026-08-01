package br.com.nsfatima.gestao.patrimonio.domain.event;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;

import java.util.UUID;

public record AtivoAlocacaoAtrasoEvent(
        UUID alocacaoId,
        UUID ativoId,
        AtivoAlocacaoTipo tipo) {
}
