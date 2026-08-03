package br.com.nsfatima.gestao.patrimonio.domain.event;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;

import java.util.UUID;

/**
 * Publicado quando uma ocupação (alocação ou manutenção) de ativo patrimonial ultrapassa a data
 * de fim sem ser devolvida, disparando notificação de atraso.
 */
public record AtivoAlocacaoAtrasoEvent(
        UUID alocacaoId,
        UUID ativoId,
        AtivoAlocacaoTipo tipo) {
}
