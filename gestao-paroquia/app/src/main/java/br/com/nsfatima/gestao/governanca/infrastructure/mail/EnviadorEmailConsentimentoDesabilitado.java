package br.com.nsfatima.gestao.governanca.infrastructure.mail;

import br.com.nsfatima.gestao.governanca.domain.port.EnviadorEmailConsentimento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adaptador padrão (desenvolvimento e testes) que não envia nada: ativo enquanto {@code app.mail.enabled}
 * não for {@code true}. Registra apenas que o envio foi suprimido — nunca o código OTP nem o destinatário —
 * de modo que ambientes sem SMTP sobem sem exigir {@code spring.mail.host}.
 */
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class EnviadorEmailConsentimentoDesabilitado implements EnviadorEmailConsentimento {

    private static final Logger LOG = LogManager.getLogger(EnviadorEmailConsentimentoDesabilitado.class);

    @Override
    public void enviarCodigoOtp(String destinatario, String codigo) {
        LOG.info("Envio de e-mail desabilitado (app.mail.enabled=false): código OTP de consentimento suprimido.");
    }

    @Override
    public void enviarComprovante(String destinatario, String tipoEvento, String protocolo) {
        LOG.info("Envio de e-mail desabilitado (app.mail.enabled=false): comprovante {} suprimido (protocolo {}).",
                tipoEvento, protocolo);
    }
}
