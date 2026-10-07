package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ValidadorHierarquiaOrganizacionalTest {

    @Mock private OrganizacaoRepository organizacaoRepository;
    @Mock private PastoralMovimentoOficialRepository pastoralOficialRepository;
    @Mock private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    private ValidadorHierarquiaOrganizacional policy;
    private final UUID matrizId = UUID.randomUUID();
    private final UUID conselhoId = UUID.randomUUID();
    private final UUID pastoralOficialId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        policy = new ValidadorHierarquiaOrganizacional(
                organizacaoRepository, pastoralOficialRepository, unidadeTerritorialRepository);

        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(matrizId);
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setAtivo(true);
        lenient().when(unidadeTerritorialRepository.findById(matrizId)).thenReturn(Optional.of(matriz));

        Organizacao conselho = new Organizacao(conselhoId, "Conselho", TipoOrganizacao.CONSELHO,
                null, null, null, true, matrizId, null);
        lenient().when(organizacaoRepository.findById(conselhoId)).thenReturn(Optional.of(conselho));

        PastoralMovimentoOficial oficial = new PastoralMovimentoOficial(pastoralOficialId, "Liturgia",
                TipoOrganizacao.PASTORAL);
        lenient().when(pastoralOficialRepository.findById(pastoralOficialId)).thenReturn(Optional.of(oficial));
    }

    @Test
    void conselhoExigeMaeMatrizOuComunidade() {
        assertThatCode(() -> policy.validar(null, TipoOrganizacao.CONSELHO, matrizId, null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.CONSELHO, null, null))
                .isInstanceOf(OrganizacaoNegocioException.class);
    }

    @Test
    void pastoralExigeConselhoComoMaeEVinculoOficialDoMesmoTipo() {
        assertThatCode(() -> policy.validar(null, TipoOrganizacao.PASTORAL, conselhoId, pastoralOficialId))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.PASTORAL, conselhoId, null))
                .isInstanceOf(OrganizacaoNegocioException.class)
                .hasMessageContaining("obrigatório");
    }

    @Test
    void naoPodeSerMaeDeSiMesma() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> policy.validar(id, TipoOrganizacao.MINISTERIO, id, null))
                .isInstanceOf(OrganizacaoNegocioException.class);
    }

    @Test
    void ministerioNaoExigeMae() {
        assertThatCode(() -> policy.validar(null, TipoOrganizacao.MINISTERIO, null, null)).doesNotThrowAnyException();
    }
}
