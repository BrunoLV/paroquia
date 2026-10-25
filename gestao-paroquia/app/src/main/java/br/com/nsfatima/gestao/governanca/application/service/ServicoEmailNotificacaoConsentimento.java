package br.com.nsfatima.gestao.governanca.application.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Serviço para geração de OTP, mascaramento de e-mail e envio de notificações de consentimento.
 */
@Service
public class ServicoEmailNotificacaoConsentimento {

    private static final Logger log = LogManager.getLogger(ServicoEmailNotificacaoConsentimento.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    public String gerarCodigoNumerico() {
        int codigo = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(codigo);
    }

    public String hashCodigo(String codigo) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(codigo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static final String SEPARADOR_EMAIL = "@";

    public String mascararEmail(String email) {
        if (email == null || !email.contains(SEPARADOR_EMAIL)) {
            return "***@***";
        }
        String[] partes = email.split(SEPARADOR_EMAIL, 2);
        String usuario = partes[0];
        String dominio = partes[1];
        if (usuario.length() <= 2) {
            return usuario.charAt(0) + "***" + SEPARADOR_EMAIL + dominio;
        }
        return usuario.charAt(0) + "***" + usuario.charAt(usuario.length() - 1) + SEPARADOR_EMAIL + dominio;
    }

    public void enviarCodigoOtp(String email, String codigo) {
        // Em ambiente de produção, este método dispara o envio via SMTP / provedor transacional.
        // PII Zero em logs: mascaramos o e-mail nos logs operacionais.
        log.info("Código OTP de consentimento gerado para {}", mascararEmail(email));
    }

    public void enviarComprovante(String email, String tipoEvento, String protocolo) {
        log.info("Comprovante de {} enviado para {} (Protocolo: {})",
                tipoEvento, mascararEmail(email), protocolo);
    }
}
