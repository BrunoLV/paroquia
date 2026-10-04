package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.AtualizarIdentidadeVisualRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para atualização das cores institucionais da paróquia no banco de dados.
 */
@Service
public class AtualizarIdentidadeVisualUseCase {

    private static final UUID DEFAULT_CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ParoquiaConfiguracaoRepository repository;
    private final ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;

    public AtualizarIdentidadeVisualUseCase(
            ParoquiaConfiguracaoRepository repository,
            ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase) {
        this.repository = repository;
        this.obterConfiguracaoParoquiaUseCase = obterConfiguracaoParoquiaUseCase;
    }

    @Transactional
    public ParoquiaConfiguracaoResponse execute(AtualizarIdentidadeVisualRequest request) {
        ParoquiaConfiguracaoEntity config = repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    ParoquiaConfiguracaoEntity nova = new ParoquiaConfiguracaoEntity();
                    nova.setId(DEFAULT_CONFIG_ID);
                    return nova;
                });

        config.setCorPrimariaHex(request.corPrimariaHex().trim());
        config.setCorSecundariaHex(request.corSecundariaHex().trim());

        repository.save(config);

        return obterConfiguracaoParoquiaUseCase.execute();
    }
}
