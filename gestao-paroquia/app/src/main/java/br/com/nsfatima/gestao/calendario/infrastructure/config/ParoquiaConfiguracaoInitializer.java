package br.com.nsfatima.gestao.calendario.infrastructure.config;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

/**
 * Garante, na inicialização da aplicação, que exista sempre uma linha de configuração da
 * paróquia ({@link ParoquiaConfiguracaoEntity}) persistida no banco, semeada a partir dos valores
 * padrão definidos em {@link ParoquiaProperties}. Isso evita que telas e relatórios que dependem
 * dessa configuração (nome, diocese, cores, logo) encontrem um estado vazio em um banco novo.
 */
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

    /**
     * Executado uma única vez na subida da aplicação: se ainda não houver nenhuma configuração
     * de paróquia persistida, cria a linha padrão com ID fixo, copiando os valores de
     * {@link ParoquiaProperties} e tentando carregar o logo a partir do classpath. Falhas ao
     * carregar o logo são silenciosamente ignoradas para não impedir a subida da aplicação.
     *
     * @param args argumentos de inicialização da aplicação Spring Boot
     */
    @Override
    public void run(ApplicationArguments args) {
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
