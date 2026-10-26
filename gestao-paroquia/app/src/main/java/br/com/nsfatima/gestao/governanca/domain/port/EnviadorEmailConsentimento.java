package br.com.nsfatima.gestao.governanca.domain.port;

/**
 * Porta de saída para as mensagens de e-mail do canal de consentimento: o código OTP de verificação de
 * titularidade e o comprovante de manifestação. A aplicação não conhece o transporte (SMTP, provedor
 * transacional ou nenhum, em desenvolvimento). Implementações nunca registram em log o código nem o
 * endereço de destino, e a falha de entrega não deve vazar para o titular: a resposta pública do portal é
 * idêntica quer o e-mail tenha sido entregue ou não.
 */
public interface EnviadorEmailConsentimento {

    /**
     * Envia o código numérico de verificação ao e-mail cadastrado do titular.
     *
     * @param destinatario e-mail cadastrado do titular
     * @param codigo código OTP em claro (existe apenas em memória e no corpo da mensagem)
     */
    void enviarCodigoOtp(String destinatario, String codigo);

    /**
     * Envia o comprovante digital de uma manifestação de consentimento ou revogação.
     *
     * @param destinatario e-mail cadastrado do titular
     * @param tipoEvento natureza da manifestação (CONCESSAO, ATUALIZACAO ou REVOGACAO)
     * @param protocolo identificador opaco do comprovante
     */
    void enviarComprovante(String destinatario, String tipoEvento, String protocolo);
}
