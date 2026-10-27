package br.com.nsfatima.gestao.governanca.api.v1.dto;

import br.com.nsfatima.gestao.governanca.domain.model.StatusSolicitacaoDpo;

import java.time.Instant;
import java.util.UUID;

/**
 * Resposta de consulta de solicitação de titular ao DPO. Na visão pública (consulta por protocolo e eco da
 * abertura) nome, e-mail e CPF vêm mascarados; a visão do DPO traz os dados completos. {@code prazoVencido}
 * e {@code diasRestantesPrazo} acompanham o prazo legal de 15 dias (Art. 19, II); este último é {@code null}
 * depois da conclusão e negativo quando o prazo já estourou.
 */
public record SolicitacaoDpoResponse(
        UUID id,
        String protocolo,
        String titularNome,
        String titularEmail,
        String titularCpf,
        String tipoSolicitacao,
        String mensagem,
        StatusSolicitacaoDpo status,
        String resposta,
        Instant prazoRespostaUtc,
        Instant respondidoEmUtc,
        Instant criadoEmUtc,
        boolean prazoVencido,
        Long diasRestantesPrazo
) {}
