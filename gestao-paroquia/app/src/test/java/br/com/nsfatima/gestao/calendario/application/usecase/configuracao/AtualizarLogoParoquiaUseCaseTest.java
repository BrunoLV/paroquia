package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarLogoParoquiaUseCase - Testes Unitários")
class AtualizarLogoParoquiaUseCaseTest {

    @Mock
    private ParoquiaConfiguracaoRepository repository;

    @Mock
    private ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;

    @InjectMocks
    private AtualizarLogoParoquiaUseCase useCase;

    @Test
    @DisplayName("Deve binarizar e salvar logotipo de fundo claro com sucesso")
    void deveSalvarLogoFundoClaroComSucesso() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setId(UUID.randomUUID());
        when(repository.findAll()).thenReturn(List.of(config));

        byte[] fakeBytes = new byte[]{1, 2, 3, 4};
        ParoquiaConfiguracaoResponse dummyResponse = new ParoquiaConfiguracaoResponse(
                config.getId(), "Paróquia", "Diocese", "Endereço", null, null, null,
                "#1A237E", "#808080", true, false, "/logo/claro", "/logo/escuro"
        );
        when(obterConfiguracaoParoquiaUseCase.execute()).thenReturn(dummyResponse);

        ParoquiaConfiguracaoResponse response = useCase.execute(VarianteFundoLogo.CLARO, fakeBytes, "image/png");

        assertThat(response).isNotNull();
        assertThat(response.possuiLogoFundoClaro()).isTrue();

        ArgumentCaptor<ParoquiaConfiguracaoEntity> captor = ArgumentCaptor.forClass(ParoquiaConfiguracaoEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLogoFundoClaroBytes()).isEqualTo(fakeBytes);
        assertThat(captor.getValue().getLogoFundoClaroTipoMime()).isEqualTo("image/png");
    }

    @Test
    @DisplayName("Deve binarizar e salvar logotipo de fundo escuro com sucesso")
    void deveSalvarLogoFundoEscuroComSucesso() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setId(UUID.randomUUID());
        when(repository.findAll()).thenReturn(List.of(config));

        byte[] fakeBytes = new byte[]{9, 8, 7};
        ParoquiaConfiguracaoResponse dummyResponse = new ParoquiaConfiguracaoResponse(
                config.getId(), "Paróquia", "Diocese", "Endereço", null, null, null,
                "#1A237E", "#808080", false, true, "/logo/claro", "/logo/escuro"
        );
        when(obterConfiguracaoParoquiaUseCase.execute()).thenReturn(dummyResponse);

        ParoquiaConfiguracaoResponse response = useCase.execute(VarianteFundoLogo.ESCURO, fakeBytes, "image/jpeg");

        assertThat(response).isNotNull();

        ArgumentCaptor<ParoquiaConfiguracaoEntity> captor = ArgumentCaptor.forClass(ParoquiaConfiguracaoEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLogoFundoEscuroBytes()).isEqualTo(fakeBytes);
        assertThat(captor.getValue().getLogoFundoEscuroTipoMime()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void deveRejeitarArquivoVazio() {
        assertThatThrownBy(() -> useCase.execute(VarianteFundoLogo.CLARO, new byte[0], "image/png"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo que excede 2MB")
    void deveRejeitarArquivoMaiorQue2MB() {
        byte[] grande = new byte[(2 * 1024 * 1024) + 1];
        assertThatThrownBy(() -> useCase.execute(VarianteFundoLogo.CLARO, grande, "image/png"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2MB");
    }

    @Test
    @DisplayName("Deve rejeitar tipo MIME não suportado")
    void deveRejeitarTipoMimeInvalido() {
        byte[] fakeBytes = new byte[]{1, 2, 3};
        assertThatThrownBy(() -> useCase.execute(VarianteFundoLogo.CLARO, fakeBytes, "application/pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não suportado");
    }
}
