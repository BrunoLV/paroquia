package br.com.nsfatima.gestao.support.infrastructure.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Componente Spring responsável por inicializar de forma segura a chave mestra de criptografia
 * simétrica estática em {@link CryptoUtils}.
 *
 * <p>A inicialização desacopla a injeção de dependências do Spring da utilização estática do utilitário
 * criptográfico em JPA Attribute Converters, garantindo que o segredo configurado em propriedades
 * de ambiente (ex: application.yml ou variáveis de ambiente) esteja devidamente carregado antes de
 * qualquer operação de persistência ou leitura no banco de dados.
 */
@Component
public class CryptoInitializer {

    static final int TAMANHO_MINIMO_CHAVE = 32;

    private final String cryptoKey;

    @Value("${app.security.cpf-pepper}")
    private String cpfPepper;

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
     * do bean, antes de qualquer operação de cifragem/decifragem. Também configura o pepper do blind index
     * de CPF ({@link CpfBlindIndex}), obrigatório e sem valor padrão: sem ele o contexto não sobe.
     */
    @PostConstruct
    public void init() {
        CryptoUtils.setSecretKey(cryptoKey);
        CpfBlindIndex.setPepper(cpfPepper);
    }
}
