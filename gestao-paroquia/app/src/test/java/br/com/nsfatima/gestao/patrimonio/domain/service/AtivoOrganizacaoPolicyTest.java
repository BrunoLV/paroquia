package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoOrganizacaoInvalidaException;
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
class AtivoOrganizacaoPolicyTest {

    @Mock
    private OrganizacaoJpaRepository organizacaoRepository;

    private AtivoOrganizacaoPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AtivoOrganizacaoPolicy(organizacaoRepository);
    }

    private OrganizacaoEntity organizacaoComTipo(TipoOrganizacao tipo) {
        OrganizacaoEntity organizacao = new OrganizacaoEntity();
        organizacao.setId(UUID.randomUUID());
        organizacao.setTipo(tipo);
        return organizacao;
    }

    @ParameterizedTest
    @EnumSource(value = TipoOrganizacao.class, names = {"MATRIZ", "COMUNIDADE"})
    void devePermitirMatrizOuComunidadeComoDona(TipoOrganizacao tipo) {
        OrganizacaoEntity organizacao = organizacaoComTipo(tipo);
        when(organizacaoRepository.findById(organizacao.getId())).thenReturn(Optional.of(organizacao));

        assertThatCode(() -> policy.assertOwnerOrganizacaoPermitida(organizacao.getId()))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = TipoOrganizacao.class, names = {"MATRIZ", "COMUNIDADE"}, mode = EnumSource.Mode.EXCLUDE)
    void deveRejeitarOutrosTiposDeOrganizacaoComoDona(TipoOrganizacao tipo) {
        OrganizacaoEntity organizacao = organizacaoComTipo(tipo);
        when(organizacaoRepository.findById(organizacao.getId())).thenReturn(Optional.of(organizacao));

        assertThatThrownBy(() -> policy.assertOwnerOrganizacaoPermitida(organizacao.getId()))
                .isInstanceOf(AtivoOrganizacaoInvalidaException.class);
    }

    @Test
    void deveRejeitarOrganizacaoInexistente() {
        UUID organizacaoId = UUID.randomUUID();
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policy.assertOwnerOrganizacaoPermitida(organizacaoId))
                .isInstanceOf(AtivoOrganizacaoInvalidaException.class);
    }
}
