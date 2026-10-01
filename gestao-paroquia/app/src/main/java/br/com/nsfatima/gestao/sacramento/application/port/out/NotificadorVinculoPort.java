package br.com.nsfatima.gestao.sacramento.application.port.out;

/**
 * Porta de notificação persistente aos Párocos quando a Secretaria propõe vínculos sacramentais que
 * aguardam aprovação. A mensagem carrega apenas contagem e tipo do sacramento, nunca dados pessoais.
 */
public interface NotificadorVinculoPort {

    /**
     * Avisa os Párocos ativos de vínculos pendentes de aprovação.
     *
     * @param autor login de quem propôs
     * @param sacramento nome do sacramento do assento
     * @param quantidade quantos vínculos novos ficaram pendentes
     */
    void notificarParocos(String autor, String sacramento, int quantidade);
}
