package br.com.nsfatima.gestao.calendario.application.usecase.configuracao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.AtualizarDpoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarDpoUseCase - Testes Unitários de Designação do Encarregado LGPD")
class AtualizarDpoUseCaseTest {

    @Mock
    private ParoquiaConfiguracaoRepository repository;

    @Mock
    private ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;

    private AtualizarDpoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AtualizarDpoUseCase(repository, obterConfiguracaoParoquiaUseCase);
    }

    @Test
    @DisplayName("Deve atualizar os dados do encarregado de dados (DPO) na configuração paroquial ativa")
    void deveAtualizarDpoComSucesso() {
        ParoquiaConfiguracaoEntity config = new ParoquiaConfiguracaoEntity();
        config.setNome("Paróquia Nossa Senhora de Fátima");
        config.setDiocese("Arquidiocese de Maringá");

        when(repository.findAll()).thenReturn(List.of(config));
        when(repository.save(any(ParoquiaConfiguracaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ParoquiaConfiguracaoResponse responseEsperada = new ParoquiaConfiguracaoResponse(
                config.getId(),
                "Paróquia Nossa Senhora de Fátima",
                "Arquidiocese de Maringá",
                null, null, null, null,
                null, null, false, false, null, null,
                "Maria Aparecida dos Santos",
                "dpo@nsfatima.org.br",
                "(44) 3028-1500"
        );
        when(obterConfiguracaoParoquiaUseCase.execute()).thenReturn(responseEsperada);

        AtualizarDpoRequest request = new AtualizarDpoRequest(
                "Maria Aparecida dos Santos",
                "dpo@nsfatima.org.br",
                "(44) 3028-1500"
        );

        ParoquiaConfiguracaoResponse response = useCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.dpoNome()).isEqualTo("Maria Aparecida dos Santos");
        assertThat(response.dpoEmail()).isEqualTo("dpo@nsfatima.org.br");
        assertThat(response.dpoTelefone()).isEqualTo("(44) 3028-1500");

        ArgumentCaptor<ParoquiaConfiguracaoEntity> captor = ArgumentCaptor.forClass(ParoquiaConfiguracaoEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDpoNome()).isEqualTo("Maria Aparecida dos Santos");
        assertThat(captor.getValue().getDpoEmail()).isEqualTo("dpo@nsfatima.org.br");
        assertThat(captor.getValue().getDpoTelefone()).isEqualTo("(44) 3028-1500");
    }
}
