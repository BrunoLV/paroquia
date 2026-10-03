package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Caso de uso para recuperação do binário do logotipo oficial da paróquia de acordo com a variante de fundo.
 */
@Service
public class ObterLogoParoquiaUseCase {

    public record LogoBinario(byte[] bytes, String contentType) {}

    private final ParoquiaConfiguracaoRepository repository;

    public ObterLogoParoquiaUseCase(ParoquiaConfiguracaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<LogoBinario> execute(VarianteFundoLogo variante) {
        return repository.findAll().stream()
                .findFirst()
                .flatMap(config -> {
                    byte[] bytes = config.getLogoBytesPorVariante(variante);
                    if (bytes == null || bytes.length == 0) {
                        return Optional.empty();
                    }
                    String mime = config.getTipoMimePorVariante(variante);
                    return Optional.of(new LogoBinario(bytes, mime));
                });
    }
}
