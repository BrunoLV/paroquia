package br.com.nsfatima.gestao.support.infrastructure.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CryptoInitializer {

    @Value("${app.security.crypto-key:default-dev-fallback-key-should-be-changed-in-prod}")
    private String cryptoKey;

    @PostConstruct
    public void init() {
        CryptoUtils.setSecretKey(cryptoKey);
    }
}
