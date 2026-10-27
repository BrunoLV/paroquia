package br.com.nsfatima.gestao.governanca.domain.model;

/**
 * Estados do ciclo de vida de contenção e tratamento de um incidente de segurança (Art. 48 LGPD).
 *
 * <p>A máquina de estados só avança: {@code EM_ANALISE → CONTIDO → NOTIFICADO → ENCERRADO}, podendo
 * pular etapas que não se aplicam (um incidente sem risco relevante é encerrado sem notificação). Nunca
 * retrocede, e {@code ENCERRADO} é final: o registro deixa de aceitar alterações, preservando-o como prova.
 */
public enum StatusIncidente {
    EM_ANALISE,
    CONTIDO,
    NOTIFICADO,
    ENCERRADO;

    /**
     * Indica se o incidente pode passar deste estado para o destino. Permanecer no mesmo estado é permitido
     * (atualização de medidas de mitigação) exceto em {@code ENCERRADO}.
     *
     * @param destino estado pretendido
     * @return {@code true} se a transição é válida
     */
    public boolean podeTransitarPara(StatusIncidente destino) {
        return this != ENCERRADO && destino.ordinal() >= this.ordinal();
    }
}
