package br.com.nsfatima.gestao.governanca.application.service;

import br.com.nsfatima.gestao.governanca.domain.service.MascaraDadosPessoais;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Regras do código OTP de consentimento: geração criptograficamente segura, hash para armazenamento e
 * mascaramento de e-mail. O envio em si pertence à porta {@code EnviadorEmailConsentimento}.
 */
@Service
public class ServicoEmailNotificacaoConsentimento {

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

    public String mascararEmail(String email) {
        return MascaraDadosPessoais.email(email);
    }
}
