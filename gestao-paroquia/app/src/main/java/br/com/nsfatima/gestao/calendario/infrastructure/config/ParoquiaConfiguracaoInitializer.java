package br.com.nsfatima.gestao.calendario.infrastructure.config;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

/**
 * Garante, na inicialização da aplicação, que exista sempre a linha de identidade visual da paróquia
 * ({@link ParoquiaConfiguracaoEntity}) persistida no banco, semeada com as cores institucionais padrão
 * e os logotipos oficiais binarizados para fundo claro e escuro.
 */
@Component
public class ParoquiaConfiguracaoInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ParoquiaConfiguracaoInitializer.class);
    private static final UUID CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String DEFAULT_COR_PRIMARIA = "#1A237E";
    private static final String DEFAULT_COR_SECUNDARIA = "#808080";
    private static final String PATH_LOGO_CLARO = "logo-paroquia-claro.png";
    private static final String PATH_LOGO_ESCURO = "logo-paroquia-escuro.png";

    private final ParoquiaConfiguracaoRepository repository;
    private final ResourceLoader resourceLoader;

    public ParoquiaConfiguracaoInitializer(
            ParoquiaConfiguracaoRepository repository,
            ResourceLoader resourceLoader) {
        this.repository = repository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) {
        ParoquiaConfiguracaoEntity config = repository.findById(CONFIG_ID).orElseGet(() -> {
            ParoquiaConfiguracaoEntity nova = new ParoquiaConfiguracaoEntity();
            nova.setId(CONFIG_ID);
            nova.setCorPrimariaHex(DEFAULT_COR_PRIMARIA);
            nova.setCorSecundariaHex(DEFAULT_COR_SECUNDARIA);
            return nova;
        });

        boolean mudou = false;

        if (config.getLogoFundoClaroBytes() == null || config.getLogoFundoClaroBytes().length == 0) {
            byte[] bytesClaro = carregarBytesDoClasspath(PATH_LOGO_CLARO);
            if (bytesClaro.length > 0) {
                config.setLogoFundoClaroBytes(bytesClaro);
                config.setLogoFundoClaroTipoMime("image/png");
                mudou = true;
            }
        }

        if (config.getLogoFundoEscuroBytes() == null || config.getLogoFundoEscuroBytes().length == 0) {
            byte[] bytesEscuro = carregarBytesDoClasspath(PATH_LOGO_ESCURO);
            if (bytesEscuro.length > 0) {
                config.setLogoFundoEscuroBytes(bytesEscuro);
                config.setLogoFundoEscuroTipoMime("image/png");
                mudou = true;
            }
        }

        if (mudou || repository.count() == 0) {
            repository.save(config);
            log.info("Identidade visual da paróquia inicializada com sucesso no banco de dados.");
        }
    }

    private byte[] carregarBytesDoClasspath(String path) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        try {
            Resource resource = resourceLoader.getResource("classpath:" + cleanPath);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    if (bytes.length > 0) {
                        return bytes;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Falha na tentativa ResourceLoader para '{}': {}", cleanPath, e.getMessage());
        }

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(cleanPath)) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                if (bytes.length > 0) {
                    return bytes;
                }
            }
        } catch (Exception e) {
            log.debug("Falha na tentativa ContextClassLoader para '{}': {}", cleanPath, e.getMessage());
        }

        try (InputStream is = getClass().getResourceAsStream("/" + cleanPath)) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                if (bytes.length > 0) {
                    return bytes;
                }
            }
        } catch (Exception e) {
            log.debug("Falha na tentativa Class.getResourceAsStream para '/{}': {}", cleanPath, e.getMessage());
        }

        log.warn("Falha ao carregar semente de imagem do classpath: '{}'", path);
        return new byte[0];
    }
}
