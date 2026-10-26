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
 *
 * <p>A chave é obrigatória e não tem valor padrão: um fallback público no código-fonte tornaria os
 * dados pessoais cifrados em repouso decifráveis por qualquer pessoa com acesso ao repositório.
 * Ausência ou chave curta derrubam o boot (fail-fast). O perfil {@code dev} e os testes declaram
 * valores explícitos próprios.
 */
@Component
public class CryptoInitializer {

    static final int TAMANHO_MINIMO_CHAVE = 32;

    private final String cryptoKey;

    public CryptoInitializer(@Value("${app.security.crypto-key:}") String cryptoKey) {
        if (cryptoKey == null || cryptoKey.isBlank() || cryptoKey.length() < TAMANHO_MINIMO_CHAVE) {
            throw new IllegalStateException(
                    "APP_CRYPTO_KEY (app.security.crypto-key) é obrigatória e deve ter ao menos "
                            + TAMANHO_MINIMO_CHAVE + " caracteres.");
        }
        this.cryptoKey = cryptoKey;
    }

    /**
     * Propaga a chave de criptografia configurada para {@link CryptoUtils} logo após a construção
     * do bean, antes de qualquer operação de cifragem/decifragem.
     */
    @PostConstruct
    public void init() {
        CryptoUtils.setSecretKey(cryptoKey);
    }
}
