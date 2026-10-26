package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Configura o pepper do blind index de CPF antes de qualquer classe de teste. O pepper de produção não tem
 * valor padrão no código; testes unitários que montam entidades sem contexto Spring (e portanto sem
 * {@link CryptoInitializer}) dependem deste valor, idêntico ao de {@code application.yml} de teste.
 */
public class PepperDeTesteExtension implements BeforeAllCallback {

    /** Pepper exclusivo de testes automatizados. */
    public static final String PEPPER = "pepper-somente-para-testes-automatizados";

    @Override
    public void beforeAll(ExtensionContext context) {
        CpfBlindIndex.setPepper(PEPPER);
    }
}
