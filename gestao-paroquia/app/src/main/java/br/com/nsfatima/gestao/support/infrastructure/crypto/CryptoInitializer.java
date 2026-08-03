package br.com.nsfatima.gestao.support.infrastructure.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Componente Spring responsável por injetar a chave de criptografia configurada externamente
 * (propriedade {@code app.security.crypto-key}) em {@link CryptoUtils} na inicialização do
 * contexto. Existe porque {@link CryptoUtils} é uma classe utilitária estática (sem estado gerido
 * pelo Spring) e por isso não pode receber a chave via injeção de dependência direta — este
 * componente é a ponte entre a configuração do Spring e o estado estático da classe utilitária.
 */
@Component
public class CryptoInitializer {

    @Value("${app.security.crypto-key:default-dev-fallback-key-should-be-changed-in-prod}")
    private String cryptoKey;

    /**
     * Propaga a chave de criptografia configurada para {@link CryptoUtils} logo após a construção
     * do bean, garantindo que a chave de produção substitua o fallback de desenvolvimento antes de
     * qualquer operação de cifragem/decifragem ocorrer.
     */
    @PostConstruct
    public void init() {
        CryptoUtils.setSecretKey(cryptoKey);
    }
}
