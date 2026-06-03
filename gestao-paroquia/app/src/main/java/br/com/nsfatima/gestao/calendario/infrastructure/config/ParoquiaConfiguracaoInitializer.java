package br.com.nsfatima.gestao.calendario.infrastructure.config;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ParoquiaConfiguracaoInitializer implements ApplicationRunner {

    private final ParoquiaConfiguracaoRepository repository;
    private final ParoquiaProperties properties;

    public ParoquiaConfiguracaoInitializer(
            ParoquiaConfiguracaoRepository repository,
            ParoquiaProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() == 0) {
            ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
            config.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            config.setNome(properties.getNome());
            config.setDiocese(properties.getDiocese());
            config.setEndereco(properties.getEndereco());
            config.setCorPrimariaHex(properties.getCorPrimariaHex());
            config.setCorSecundariaHex(properties.getCorSecundariaHex());

            byte[] logoBytes = null;
            String logoPath = properties.getLogoPath();
            if (logoPath != null && !logoPath.isBlank()) {
                try (InputStream is = getClass().getResourceAsStream(logoPath)) {
                    if (is != null) {
                        logoBytes = is.readAllBytes();
                    }
                } catch (Exception e) {
                    // Silently ignore to guarantee resilience if logo file doesn't exist
                }
            }
            config.setLogoBytes(logoBytes);

            repository.save(config);
        }
    }
}
