package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.application.port.out.ConsultaInstitucionalPort;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ObterConfiguracaoParoquiaUseCase - Testes Unitários")
class ObterConfiguracaoParoquiaUseCaseTest {

    @Mock
    private ParoquiaConfiguracaoRepository paroquiaConfigRepository;

    @Mock
    private ConsultaInstitucionalPort consultaInstitucionalPort;

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private ObterConfiguracaoParoquiaUseCase useCase;

    @Test
    @DisplayName("Deve consolidar dados da Matriz, Diocese e Identidade Visual com sucesso")
    void deveConsolidarDadosComSucesso() {
        ConsultaInstitucionalPort.DadosInstitucionais dados = new ConsultaInstitucionalPort.DadosInstitucionais(
                "Paróquia Teste",
                "Diocese Teste",
                "Rua Teste, 100",
                "12.345.678/0001-90",
                "(11) 99999-8888",
                "teste@paroquia.com"
        );
        when(consultaInstitucionalPort.obterDadosInstitucionais()).thenReturn(dados);

        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setId(UUID.randomUUID());
        config.setCorPrimariaHex("#001122");
        config.setCorSecundariaHex("#334455");
        config.setLogoFundoClaroBytes(new byte[]{1, 2, 3});
        config.setLogoFundoEscuroBytes(new byte[]{4, 5, 6});
        when(paroquiaConfigRepository.findAll()).thenReturn(List.of(config));

        ParoquiaConfiguracaoResponse response = useCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(config.getId());
        assertThat(response.nome()).isEqualTo("Paróquia Teste");
        assertThat(response.diocese()).isEqualTo("Diocese Teste");
        assertThat(response.endereco()).isEqualTo("Rua Teste, 100");
        assertThat(response.cnpj()).isEqualTo("12.345.678/0001-90");
        assertThat(response.telefone()).isEqualTo("(11) 99999-8888");
        assertThat(response.email()).isEqualTo("teste@paroquia.com");
        assertThat(response.corPrimariaHex()).isEqualTo("#001122");
        assertThat(response.corSecundariaHex()).isEqualTo("#334455");
        assertThat(response.possuiLogoFundoClaro()).isTrue();
        assertThat(response.possuiLogoFundoEscuro()).isTrue();
        assertThat(response.urlLogoFundoClaro()).isEqualTo("/api/v1/configuracao-paroquia/logo/claro");
        assertThat(response.urlLogoFundoEscuro()).isEqualTo("/api/v1/configuracao-paroquia/logo/escuro");
    }

    @Test
    @DisplayName("Deve usar valores padrão de cores quando configuração não existir no banco")
    void deveUsarValoresPadraoQuandoConfiguracaoNaoExistir() {
        ConsultaInstitucionalPort.DadosInstitucionais dados = ConsultaInstitucionalPort.DadosInstitucionais.padrao();
        when(consultaInstitucionalPort.obterDadosInstitucionais()).thenReturn(dados);
        when(paroquiaConfigRepository.findAll()).thenReturn(List.of());

        ParoquiaConfiguracaoResponse response = useCase.execute();

        assertThat(response).isNotNull();
        assertThat(response.corPrimariaHex()).isEqualTo("#1A237E");
        assertThat(response.corSecundariaHex()).isEqualTo("#808080");
        assertThat(response.possuiLogoFundoClaro()).isFalse();
        assertThat(response.possuiLogoFundoEscuro()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar possuiLogo true se houver imagem no classpath via ResourceLoader")
    void deveRetornarPossuiLogoTrueSeHouverNoClasspath() {
        ConsultaInstitucionalPort.DadosInstitucionais dados = ConsultaInstitucionalPort.DadosInstitucionais.padrao();
        when(consultaInstitucionalPort.obterDadosInstitucionais()).thenReturn(dados);
        when(paroquiaConfigRepository.findAll()).thenReturn(List.of());

        Resource mockResource = mock(Resource.class);
        when(mockResource.exists()).thenReturn(true);
        when(resourceLoader.getResource("classpath:logo-paroquia-claro.png")).thenReturn(mockResource);
        when(resourceLoader.getResource("classpath:logo-paroquia-escuro.png")).thenReturn(mockResource);

        ParoquiaConfiguracaoResponse response = useCase.execute();

        assertThat(response.possuiLogoFundoClaro()).isTrue();
        assertThat(response.possuiLogoFundoEscuro()).isTrue();
    }
}
