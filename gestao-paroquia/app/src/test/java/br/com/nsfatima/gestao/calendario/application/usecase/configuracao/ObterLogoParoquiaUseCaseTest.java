package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ObterLogoParoquiaUseCase - Testes Unitários")
class ObterLogoParoquiaUseCaseTest {

    @Mock
    private ParoquiaConfiguracaoRepository repository;

    @InjectMocks
    private ObterLogoParoquiaUseCase useCase;

    @Test
    @DisplayName("Deve retornar bytes e contentType da variante clara")
    void deveRetornarLogoClaro() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setLogoFundoClaroBytes(new byte[]{1, 2, 3});
        config.setLogoFundoClaroTipoMime("image/png");
        when(repository.findAll()).thenReturn(List.of(config));

        Optional<ObterLogoParoquiaUseCase.LogoBinario> result = useCase.execute(VarianteFundoLogo.CLARO);

        assertThat(result).isPresent();
        assertThat(result.get().bytes()).isEqualTo(new byte[]{1, 2, 3});
        assertThat(result.get().contentType()).isEqualTo("image/png");
    }

    @Test
    @DisplayName("Deve retornar fallback para logo claro quando logo escuro não for cadastrado")
    void deveRetornarFallbackParaLogoClaro() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setLogoFundoClaroBytes(new byte[]{1, 2, 3});
        config.setLogoFundoClaroTipoMime("image/png");
        config.setLogoFundoEscuroBytes(null);
        when(repository.findAll()).thenReturn(List.of(config));

        Optional<ObterLogoParoquiaUseCase.LogoBinario> result = useCase.execute(VarianteFundoLogo.ESCURO);

        assertThat(result).isPresent();
        assertThat(result.get().bytes()).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("Deve retornar vazio quando nenhum logo estiver cadastrado")
    void deveRetornarVazioQuandoNaoCadastrado() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        when(repository.findAll()).thenReturn(List.of(config));

        Optional<ObterLogoParoquiaUseCase.LogoBinario> result = useCase.execute(VarianteFundoLogo.CLARO);

        assertThat(result).isEmpty();
    }
}
