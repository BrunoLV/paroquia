package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.AtualizarIdentidadeVisualRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarIdentidadeVisualUseCase - Testes Unitários")
class AtualizarIdentidadeVisualUseCaseTest {

    @Mock
    private ParoquiaConfiguracaoRepository repository;

    @Mock
    private ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;

    @InjectMocks
    private AtualizarIdentidadeVisualUseCase useCase;

    @Test
    @DisplayName("Deve atualizar cores primária e secundária com sucesso")
    void deveAtualizarCoresComSucesso() {
        ParoquiaConfiguracaoEntity configExistente = new ParoquiaConfiguracaoEntity();
        configExistente.setId(UUID.randomUUID());
        configExistente.setCorPrimariaHex("#111111");
        configExistente.setCorSecundariaHex("#222222");

        when(repository.findAll()).thenReturn(List.of(configExistente));

        AtualizarIdentidadeVisualRequest request = new AtualizarIdentidadeVisualRequest("#AABBCC", "#DDEEFF");

        ParoquiaConfiguracaoResponse dummyResponse = new ParoquiaConfiguracaoResponse(
                configExistente.getId(), "Paróquia", "Diocese", "Endereço", null, null, null,
                "#AABBCC", "#DDEEFF", false, false, "/logo/claro", "/logo/escuro"
        );
        when(obterConfiguracaoParoquiaUseCase.execute()).thenReturn(dummyResponse);

        ParoquiaConfiguracaoResponse result = useCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.corPrimariaHex()).isEqualTo("#AABBCC");

        ArgumentCaptor<ParoquiaConfiguracaoEntity> captor = ArgumentCaptor.forClass(ParoquiaConfiguracaoEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCorPrimariaHex()).isEqualTo("#AABBCC");
        assertThat(captor.getValue().getCorSecundariaHex()).isEqualTo("#DDEEFF");
    }
}
