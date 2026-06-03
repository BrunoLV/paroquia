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

    public ParoquiaConfiguracaoInitializer(ParoquiaConfiguracaoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() == 0) {
            ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
            config.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            config.setNome("Paróquia Nossa Senhora de Fátima");
            config.setDiocese("Diocese de São Miguel Paulista");
            config.setEndereco("Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP");
            config.setCorPrimariaHex("#1A237E");
            config.setCorSecundariaHex("#808080");

            byte[] logoBytes = null;
            try (InputStream is = getClass().getResourceAsStream("/logo-paroquia.png")) {
                if (is != null) {
                    logoBytes = is.readAllBytes();
                }
            } catch (Exception e) {
                // Silently ignore to guarantee resilience if logo file doesn't exist
            }
            config.setLogoBytes(logoBytes);

            repository.save(config);
        }
    }
}
