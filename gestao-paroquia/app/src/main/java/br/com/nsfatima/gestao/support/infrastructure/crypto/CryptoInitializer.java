package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class CryptoInitializer {

    @Value("${app.security.crypto-key:default-dev-fallback-key-should-be-changed-in-prod}")
    private String cryptoKey;

    @PostConstruct
    public void init() {
        CryptoUtils.setSecretKey(cryptoKey);
    }
}
