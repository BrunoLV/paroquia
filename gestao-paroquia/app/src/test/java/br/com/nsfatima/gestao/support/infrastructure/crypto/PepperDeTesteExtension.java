package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Configura o pepper do blind index de CPF e a chave de cifragem antes de qualquer classe de teste.
 * O pepper e a chave de produção não têm valor padrão no código; testes unitários que montam entidades sem
 * contexto Spring (e portanto sem {@link CryptoInitializer}) dependem destes valores, idênticos aos de
 * {@code application.yml} de teste.
 */
public class PepperDeTesteExtension implements BeforeAllCallback {

    /** Chave exclusiva de testes automatizados (espelha application.yml de teste). */
    public static final String CHAVE = "chave-de-criptografia-exclusiva-para-testes-automatizados";

    /** Pepper exclusivo de testes automatizados (espelha application.yml de teste). */
    public static final String PEPPER = "pepper-somente-para-testes-automatizados";

    @Override
    public void beforeAll(ExtensionContext context) {
        CryptoUtils.setSecretKey(CHAVE);
        CpfBlindIndex.setPepper(PEPPER);
    }
}
