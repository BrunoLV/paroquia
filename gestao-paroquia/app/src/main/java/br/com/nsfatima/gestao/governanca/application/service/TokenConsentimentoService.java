package br.com.nsfatima.gestao.governanca.application.service;

import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Emissor e validador de tokens efêmeros de sessão de consentimento (HMAC-SHA256). O token carrega o
 * identificador do desafio OTP validado ({@code jti}); o caso de uso o consome uma única vez, de modo que um
 * token interceptado ou reenviado não possa registrar uma segunda manifestação.
 */
@Service
public class TokenConsentimentoService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long DURACAO_MINUTOS = 15;
    private static final String SEPARADOR = ":";

    private final byte[] secretKey;

    public record TokenPayload(TipoTitularConsentimento titularTipo, UUID titularId, UUID jti) {}

    public TokenConsentimentoService(
            @Value("${app.security.consent-secret:paroquia-consent-jwt-secret-key-lgpd-2026}") String secret) {
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String gerarToken(TipoTitularConsentimento titularTipo, UUID titularId, UUID jti) {
        long expiraEm = Instant.now().plusSeconds(DURACAO_MINUTOS * 60).toEpochMilli();
        String dados = titularTipo.name() + SEPARADOR + titularId + SEPARADOR + expiraEm + SEPARADOR + jti;
        String assinatura = assinar(dados);
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(dados.getBytes(StandardCharsets.UTF_8));
        return payloadBase64 + "." + assinatura;
    }

    public TokenPayload validarToken(String token) {
        if (token == null || !token.contains(".")) {
            throw new IllegalArgumentException("Token de sessão de consentimento inválido ou ausente.");
        }
        String[] partes = token.split("\\.");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Estrutura do token de consentimento inválida.");
        }
        String dados;
        try {
            dados = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Payload do token corrompido.", e);
        }
        String assinaturaEsperada = assinar(dados);
        byte[] esperada = assinaturaEsperada.getBytes(StandardCharsets.UTF_8);
        byte[] recebida = partes[1].getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(esperada, recebida)) {
            throw new IllegalArgumentException("Assinatura do token de consentimento inválida.");
        }
        String[] campos = dados.split(SEPARADOR);
        if (campos.length != 4) {
            throw new IllegalArgumentException("Campos do token corrompidos.");
        }
        long expiraEm = Long.parseLong(campos[2]);
        if (Instant.now().toEpochMilli() > expiraEm) {
            throw new IllegalArgumentException("Token de sessão expirado. Solicite novo código de validação.");
        }
        TipoTitularConsentimento tipo = TipoTitularConsentimento.valueOf(campos[0]);
        UUID titularId = UUID.fromString(campos[1]);
        return new TokenPayload(tipo, titularId, UUID.fromString(campos[3]));
    }

    private String assinar(String dados) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secretKey, HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(dados.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao calcular HMAC do token", e);
        }
    }
}
