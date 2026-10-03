package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Optional;

/**
 * Caso de uso para recuperação do binário do logotipo oficial da paróquia de acordo com a variante de fundo.
 */
@Service
public class ObterLogoParoquiaUseCase {

    public record LogoBinario(byte[] bytes, String contentType) {}

    private static final String DEFAULT_PNG_MIME = "image/png";

    private final ParoquiaConfiguracaoRepository repository;
    private final ResourceLoader resourceLoader;

    public ObterLogoParoquiaUseCase(
            ParoquiaConfiguracaoRepository repository,
            ResourceLoader resourceLoader) {
        this.repository = repository;
        this.resourceLoader = resourceLoader;
    }

    @Transactional(readOnly = true)
    public Optional<LogoBinario> execute(VarianteFundoLogo variante) {
        Optional<LogoBinario> logoBanco = repository.findAll().stream()
                .findFirst()
                .flatMap(config -> {
                    byte[] bytes = config.getLogoBytesPorVariante(variante);
                    if (bytes == null || bytes.length == 0) {
                        return Optional.empty();
                    }
                    String mime = config.getTipoMimePorVariante(variante);
                    return Optional.of(new LogoBinario(bytes, mime));
                });

        if (logoBanco.isPresent()) {
            return logoBanco;
        }

        byte[] fallbackBytes = carregarBytesClasspath(variante);
        if (fallbackBytes.length > 0) {
            return Optional.of(new LogoBinario(fallbackBytes, DEFAULT_PNG_MIME));
        }

        return Optional.empty();
    }

    private byte[] carregarBytesClasspath(VarianteFundoLogo variante) {
        String fileName = variante == VarianteFundoLogo.ESCURO
                ? "logo-paroquia-escuro.png"
                : "logo-paroquia-claro.png";

        try {
            Resource resource = resourceLoader.getResource("classpath:" + fileName);
            if (resource != null && resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    if (bytes.length > 0) {
                        return bytes;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return new byte[0];
    }
}
