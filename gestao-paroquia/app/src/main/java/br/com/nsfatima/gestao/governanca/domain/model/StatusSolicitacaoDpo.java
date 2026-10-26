package br.com.nsfatima.gestao.governanca.domain.model;

/**
 * Status do ciclo de vida de uma solicitação de exercício de direitos dos titulares (DSAR).
 *
 * <p>Fluxo: {@code RECEBIDA → EM_ANALISE → (ATENDIDA | REJEITADA)}. A análise pode ser pulada quando o DPO
 * responde de imediato. {@code ATENDIDA} e {@code REJEITADA} são finais: uma solicitação concluída nunca é
 * reaberta, porque a resposta ao titular é ato formal no prazo do Art. 19, II da LGPD — um novo pedido gera
 * novo protocolo.
 */
public enum StatusSolicitacaoDpo {
    RECEBIDA,
    EM_ANALISE,
    ATENDIDA,
    REJEITADA;

    /**
     * Indica se a solicitação já foi concluída e não aceita mais respostas.
     *
     * @return {@code true} para {@code ATENDIDA} e {@code REJEITADA}
     */
    public boolean isFinal() {
        return this == ATENDIDA || this == REJEITADA;
    }

    /**
     * Indica se o DPO pode registrar uma resposta que leve a solicitação deste estado ao destino. Permanecer
     * em {@code EM_ANALISE} é permitido (parecer parcial); {@code RECEBIDA} nunca é destino.
     *
     * @param destino estado pretendido pela resposta
     * @return {@code true} se a transição é válida
     */
    public boolean podeTransitarPara(StatusSolicitacaoDpo destino) {
        return !isFinal() && destino != RECEBIDA;
    }
}
