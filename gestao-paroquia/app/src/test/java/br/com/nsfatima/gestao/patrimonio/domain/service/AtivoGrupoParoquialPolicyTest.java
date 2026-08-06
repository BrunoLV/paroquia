package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtivoGrupoParoquialPolicyTest {

    @Mock
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    private AtivoGrupoParoquialPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AtivoGrupoParoquialPolicy(unidadeTerritorialRepository);
    }

    private UnidadeTerritorialEntity unidadeComTipo(TipoUnidadeTerritorial tipo) {
        UnidadeTerritorialEntity unidade = new UnidadeTerritorialEntity();
        unidade.setId(UUID.randomUUID());
        unidade.setTipo(tipo);
        return unidade;
    }

    @ParameterizedTest
    @EnumSource(value = TipoUnidadeTerritorial.class, names = {"MATRIZ", "COMUNIDADE"})
    void devePermitirMatrizOuComunidadeComoDona(TipoUnidadeTerritorial tipo) {
        UnidadeTerritorialEntity unidade = unidadeComTipo(tipo);
        when(unidadeTerritorialRepository.findById(unidade.getId())).thenReturn(Optional.of(unidade));

        assertThatCode(() -> policy.assertGrupoParoquialProprietariaPermitida(unidade.getId()))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TipoUnidadeTerritorial.class, names = {"MATRIZ", "COMUNIDADE"}, mode = EnumSource.Mode.EXCLUDE)
    void deveRejeitarOutrosTiposDeUnidadeComoDona(TipoUnidadeTerritorial tipo) {
        UnidadeTerritorialEntity unidade = unidadeComTipo(tipo);
        when(unidadeTerritorialRepository.findById(unidade.getId())).thenReturn(Optional.of(unidade));

        assertThatThrownBy(() -> policy.assertGrupoParoquialProprietariaPermitida(unidade.getId()))
                .isInstanceOf(AtivoGrupoParoquialInvalidaException.class);
    }

    @Test
    void deveRejeitarUnidadeInexistente() {
        UUID unidadeId = UUID.randomUUID();
        when(unidadeTerritorialRepository.findById(unidadeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policy.assertGrupoParoquialProprietariaPermitida(unidadeId))
                .isInstanceOf(AtivoGrupoParoquialInvalidaException.class);
    }
}
