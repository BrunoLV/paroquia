package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Caso de uso para binarização e persistência do logotipo oficial da paróquia no banco de dados,
 * suportando variantes específicas para fundo claro ou fundo escuro.
 */
@Service
public class AtualizarLogoParoquiaUseCase {

    private static final UUID DEFAULT_CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/svg+xml"
    );

    private final ParoquiaConfiguracaoRepository repository;
    private final ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;

    public AtualizarLogoParoquiaUseCase(
            ParoquiaConfiguracaoRepository repository,
            ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase) {
        this.repository = repository;
        this.obterConfiguracaoParoquiaUseCase = obterConfiguracaoParoquiaUseCase;
    }

    @Transactional
    public ParoquiaConfiguracaoResponse execute(VarianteFundoLogo variante, byte[] bytes, String contentType) {
        Objects.requireNonNull(variante, "Variante de logotipo é obrigatória.");

        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("O arquivo de logotipo não pode ser vazio.");
        }

        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("O arquivo de logotipo excede o tamanho máximo permitido de 2MB.");
        }

        String mime = contentType != null ? contentType.trim().toLowerCase() : "image/png";
        if (!ALLOWED_MIME_TYPES.contains(mime)) {
            throw new IllegalArgumentException(
                    "Tipo de mídia '" + mime + "' não suportado. Formatos aceitos: PNG, JPEG, WebP e SVG.");
        }

        ParoquiaConfiguracaoEntity config = repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    ParoquiaConfiguracaoEntity nova = new ParoquiaConfiguracaoEntity();
                    nova.setId(DEFAULT_CONFIG_ID);
                    return nova;
                });

        if (variante == VarianteFundoLogo.CLARO) {
            config.setLogoFundoClaroBytes(bytes);
            config.setLogoFundoClaroTipoMime(mime);
        } else {
            config.setLogoFundoEscuroBytes(bytes);
            config.setLogoFundoEscuroTipoMime(mime);
        }

        repository.save(config);

        return obterConfiguracaoParoquiaUseCase.execute();
    }
}
