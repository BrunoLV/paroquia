package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.AmbitoOrganismo;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.ReconhecimentoEstatuto;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
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

import java.time.LocalDate;
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
    private final UUID caepId = UUID.randomUUID();
    private final UUID pastoralOficialId = UUID.randomUUID();
    private final UUID movimentoSemEstatutoId = UUID.randomUUID();
    private final UUID movimentoReconhecidoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        policy = new ValidadorHierarquiaOrganizacional(
                organizacaoRepository, pastoralOficialRepository, unidadeTerritorialRepository);

        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(matrizId);
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setAtivo(true);
        lenient().when(unidadeTerritorialRepository.findById(matrizId)).thenReturn(Optional.of(matriz));

        Organizacao cpp = new Organizacao(conselhoId, "CPP", TipoOrganizacao.CONSELHO,
                null, null, null, true, matrizId, null, null, SubtipoConselho.CPP);
        lenient().when(organizacaoRepository.findById(conselhoId)).thenReturn(Optional.of(cpp));
        Organizacao caep = new Organizacao(caepId, "CAEP", TipoOrganizacao.CONSELHO,
                null, null, null, true, matrizId, null, null, SubtipoConselho.CAEP);
        lenient().when(organizacaoRepository.findById(caepId)).thenReturn(Optional.of(caep));

        lenient().when(pastoralOficialRepository.findById(pastoralOficialId)).thenReturn(Optional.of(
                new PastoralMovimentoOficial(pastoralOficialId, "Liturgia", TipoOrganizacao.PASTORAL)));
        lenient().when(pastoralOficialRepository.findById(movimentoSemEstatutoId)).thenReturn(Optional.of(
                new PastoralMovimentoOficial(movimentoSemEstatutoId, "Movimento X", TipoOrganizacao.MOVIMENTO)));
        lenient().when(pastoralOficialRepository.findById(movimentoReconhecidoId)).thenReturn(Optional.of(
                new PastoralMovimentoOficial(movimentoReconhecidoId, "Movimento Y", TipoOrganizacao.MOVIMENTO,
                        AmbitoOrganismo.DIOCESANO,
                        new ReconhecimentoEstatuto("Bispo diocesano", LocalDate.of(2020, 1, 1), "Decreto 1/2020"))));
    }

    @Test
    void conselhoExigeMaeTerritorialESubtipo() {
        assertThatCode(() -> policy.validar(null, TipoOrganizacao.CONSELHO, matrizId, null, null,
                SubtipoConselho.CPP)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.CONSELHO, null, null, null,
                SubtipoConselho.CPP)).isInstanceOf(OrganizacaoNegocioException.class);
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.CONSELHO, matrizId, null, null, null))
                .isInstanceOf(OrganizacaoNegocioException.class).hasMessageContaining("Subtipo");
    }

    @Test
    void pastoralExigeCppMaeVinculoOficialESetor() {
        assertThatCode(() -> policy.validar(null, TipoOrganizacao.PASTORAL, conselhoId, pastoralOficialId,
                SetorPastoral.FORMACAO_ENSINAR, null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.PASTORAL, conselhoId, null,
                SetorPastoral.FORMACAO_ENSINAR, null))
                .isInstanceOf(OrganizacaoNegocioException.class).hasMessageContaining("obrigatório");
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.PASTORAL, conselhoId, pastoralOficialId,
                null, null))
                .isInstanceOf(OrganizacaoNegocioException.class).hasMessageContaining("Setor pastoral");
    }

    @Test
    void organismoPastoralNaoPodeSerFilhoDoConselhoEconomico() {
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.PASTORAL, caepId, pastoralOficialId,
                SetorPastoral.SOCIAL_CARIDADE, null))
                .isInstanceOf(OrganizacaoNegocioException.class).hasMessageContaining("CAEP");
    }

    @Test
    void movimentoExigeEstatutoReconhecido() {
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.MOVIMENTO, conselhoId,
                movimentoSemEstatutoId, null, null))
                .isInstanceOf(OrganizacaoNegocioException.class).hasMessageContaining("estatuto reconhecido");
        assertThatCode(() -> policy.validar(null, TipoOrganizacao.MOVIMENTO, conselhoId,
                movimentoReconhecidoId, null, null)).doesNotThrowAnyException();
    }

    @Test
    void servicoParoquialExigeConselhoPastoralESetor() {
        assertThatCode(() -> policy.validar(null, TipoOrganizacao.SERVICO_PAROQUIAL, conselhoId, null,
                SetorPastoral.SANTIFICACAO_LITURGIA, null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.validar(null, TipoOrganizacao.SERVICO_PAROQUIAL, null, null,
                SetorPastoral.SANTIFICACAO_LITURGIA, null)).isInstanceOf(OrganizacaoNegocioException.class);
    }

    @Test
    void naoPodeSerMaeDeSiMesma() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> policy.validar(id, TipoOrganizacao.SERVICO_PAROQUIAL, id, null,
                SetorPastoral.SANTIFICACAO_LITURGIA, null)).isInstanceOf(OrganizacaoNegocioException.class);
    }
}
