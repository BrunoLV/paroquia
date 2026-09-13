package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.GrupoParoquialNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.GrupoParoquial;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.GrupoParoquialRepository;
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

    @Mock private GrupoParoquialRepository grupoParoquialRepository;
    @Mock private PastoralMovimentoOficialRepository pastoralOficialRepository;
    @Mock private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    private ValidadorHierarquiaOrganizacional policy;
    private final UUID matrizId = UUID.randomUUID();
    private final UUID conselhoId = UUID.randomUUID();
    private final UUID pastoralOficialId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        policy = new ValidadorHierarquiaOrganizacional(
                grupoParoquialRepository, pastoralOficialRepository, unidadeTerritorialRepository);

        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(matrizId);
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setAtivo(true);
        lenient().when(unidadeTerritorialRepository.findById(matrizId)).thenReturn(Optional.of(matriz));

        GrupoParoquial conselho = new GrupoParoquial(conselhoId, "Conselho", TipoGrupo.CONSELHO,
                null, null, null, true, matrizId, null);
        lenient().when(grupoParoquialRepository.findById(conselhoId)).thenReturn(Optional.of(conselho));

        PastoralMovimentoOficial oficial = new PastoralMovimentoOficial(pastoralOficialId, "Liturgia",
                TipoGrupo.PASTORAL);
        lenient().when(pastoralOficialRepository.findById(pastoralOficialId)).thenReturn(Optional.of(oficial));
    }

    @Test
    void conselhoExigeMaeMatrizOuComunidade() {
        assertThatCode(() -> policy.validar(null, TipoGrupo.CONSELHO, matrizId, null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validar(null, TipoGrupo.CONSELHO, null, null))
                .isInstanceOf(GrupoParoquialNegocioException.class);
    }

    @Test
    void pastoralExigeConselhoComoMaeEVinculoOficialDoMesmoTipo() {
        assertThatCode(() -> policy.validar(null, TipoGrupo.PASTORAL, conselhoId, pastoralOficialId))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validar(null, TipoGrupo.PASTORAL, conselhoId, null))
                .isInstanceOf(GrupoParoquialNegocioException.class)
                .hasMessageContaining("obrigatório");
    }

    @Test
    void naoPodeSerMaeDeSiMesma() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> policy.validar(id, TipoGrupo.MINISTERIO, id, null))
                .isInstanceOf(GrupoParoquialNegocioException.class);
    }

    @Test
    void ministerioNaoExigeMae() {
        assertThatCode(() -> policy.validar(null, TipoGrupo.MINISTERIO, null, null)).doesNotThrowAnyException();
    }
}
