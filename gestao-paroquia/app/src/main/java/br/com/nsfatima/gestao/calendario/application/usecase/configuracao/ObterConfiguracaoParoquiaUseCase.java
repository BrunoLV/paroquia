package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.application.port.out.ConsultaInstitucionalPort;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para obter a configuração e identidade visual consolidada da paróquia.
 * Agrega os dados civis da Matriz e dados canônicos da Diocese com as cores e logotipos
 * persistidos na configuração visual da aplicação.
 */
@Service
public class ObterConfiguracaoParoquiaUseCase {

    private static final UUID DEFAULT_CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String DEFAULT_COR_PRIMARIA = "#1A237E";
    private static final String DEFAULT_COR_SECUNDARIA = "#808080";

    private final ParoquiaConfiguracaoRepository paroquiaConfigRepository;
    private final ConsultaInstitucionalPort consultaInstitucionalPort;
    private final ResourceLoader resourceLoader;

    public ObterConfiguracaoParoquiaUseCase(
            ParoquiaConfiguracaoRepository paroquiaConfigRepository,
            ConsultaInstitucionalPort consultaInstitucionalPort,
            ResourceLoader resourceLoader) {
        this.paroquiaConfigRepository = paroquiaConfigRepository;
        this.consultaInstitucionalPort = consultaInstitucionalPort;
        this.resourceLoader = resourceLoader;
    }

    @Transactional(readOnly = true)
    public ParoquiaConfiguracaoResponse execute() {
        ParoquiaConfiguracaoEntity config = paroquiaConfigRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        ConsultaInstitucionalPort.DadosInstitucionais dados = consultaInstitucionalPort.obterDadosInstitucionais();
        UUID configId = config != null ? config.getId() : DEFAULT_CONFIG_ID;
        String corPrimaria = config != null && config.getCorPrimariaHex() != null
                ? config.getCorPrimariaHex()
                : DEFAULT_COR_PRIMARIA;
        String corSecundaria = config != null && config.getCorSecundariaHex() != null
                ? config.getCorSecundariaHex()
                : DEFAULT_COR_SECUNDARIA;

        boolean temClaro = (config != null && config.getLogoFundoClaroBytes() != null
                && config.getLogoFundoClaroBytes().length > 0) || existeNoClasspath("logo-paroquia-claro.png");
        boolean temEscuro = (config != null && config.getLogoFundoEscuroBytes() != null
                && config.getLogoFundoEscuroBytes().length > 0) || existeNoClasspath("logo-paroquia-escuro.png");

        return new ParoquiaConfiguracaoResponse(
                configId,
                dados.nome(),
                dados.diocese(),
                dados.endereco(),
                dados.cnpj(),
                dados.telefone(),
                dados.email(),
                corPrimaria,
                corSecundaria,
                temClaro,
                temEscuro,
                "/api/v1/configuracao-paroquia/logo/claro",
                "/api/v1/configuracao-paroquia/logo/escuro"
        );
    }

    private boolean existeNoClasspath(String path) {
        try {
            Resource r = resourceLoader.getResource("classpath:" + path);
            return r != null && r.exists();
        } catch (Exception e) {
            return false;
        }
    }
}
