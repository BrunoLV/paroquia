package br.com.nsfatima.gestao.aprovacao.application.dto;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Payload da ação de negócio pendente associada a uma solicitação de aprovação. Serializado em JSON (via
 * {@code AprovacaoAcaoPayloadMapper}) e armazenado em {@code AprovacaoEntity#actionPayloadJson} enquanto a
 * aprovação está PENDENTE, para que a ação (criação/atualização/cancelamento de evento, etc.) possa ser
 * efetivada posteriormente por uma {@code EstrategiaExecucaoAprovacao} sem depender do estado original da
 * requisição HTTP que a originou.
 */
public record AprovacaoAcaoPayload(
        String idempotencyKey,
        UUID eventoId,
        String titulo,
        String descricao,
        CategoriaEvento categoria,
        UUID organizacaoResponsavelId,
        UUID projetoId,
        Instant inicio,
        Instant fim,
        EventoStatusInput status,
        String adicionadoExtraJustificativa,
        String canceladoMotivo,
        String motivo,
        List<UUID> participantes,
        UUID localId,
        UUID igrejaVinculoId,
        UUID ativoId,
        String ativoNome) {

    public AprovacaoAcaoPayload(
            String idempotencyKey,
            UUID eventoId,
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            String motivo,
            List<UUID> participantes,
            UUID localId) {
        // Construtor de compatibilidade para chamadores que não lidam com alocação de ativo patrimonial.
        this(idempotencyKey,
                eventoId, titulo, descricao, categoria, organizacaoResponsavelId, projetoId, inicio, fim, status,
                adicionadoExtraJustificativa, canceladoMotivo, motivo, participantes, localId, null, null, null);

    }
}
