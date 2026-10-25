package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.AtualizarDpoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para atualização dos dados institucionais do Encarregado de Proteção de Dados (DPO) (Art. 41 LGPD).
 */
@Service
public class AtualizarDpoUseCase {

    private static final UUID DEFAULT_CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ParoquiaConfiguracaoRepository repository;
    private final ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;

    public AtualizarDpoUseCase(
            ParoquiaConfiguracaoRepository repository,
            ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase) {
        this.repository = repository;
        this.obterConfiguracaoParoquiaUseCase = obterConfiguracaoParoquiaUseCase;
    }

    @Transactional
    public ParoquiaConfiguracaoResponse execute(AtualizarDpoRequest request) {
        ParoquiaConfiguracaoEntity config = repository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    ParoquiaConfiguracaoEntity nova = new ParoquiaConfiguracaoEntity();
                    nova.setId(DEFAULT_CONFIG_ID);
                    return nova;
                });

        config.setDpoNome(request.dpoNome() != null ? request.dpoNome().trim() : null);
        config.setDpoEmail(request.dpoEmail() != null ? request.dpoEmail().trim().toLowerCase() : null);
        config.setDpoTelefone(request.dpoTelefone() != null ? request.dpoTelefone().trim() : null);

        repository.save(config);

        return obterConfiguracaoParoquiaUseCase.execute();
    }
}
