package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPeriodoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtivoAlocacaoConflitoPolicyTest {

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    private AtivoAlocacaoConflitoPolicy policy;
    private UUID ativoId;

    @BeforeEach
    void setUp() {
        policy = new AtivoAlocacaoConflitoPolicy(alocacaoRepository);
        ativoId = UUID.randomUUID();
    }

    private AtivoAlocacaoEntity ocupacao(LocalDate inicio, LocalDate fim, AtivoAlocacaoStatus status) {
        AtivoAlocacaoEntity entity = new AtivoAlocacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setAtivoId(ativoId);
        entity.setTipo(AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        entity.setDataInicio(inicio);
        entity.setDataFim(fim);
        entity.setStatus(status);
        return entity;
    }

    private void mockOcupacoesExistentes(AtivoAlocacaoEntity... ocupacoes) {
        when(alocacaoRepository.findByAtivoIdAndStatusIn(eq(ativoId), any()))
                .thenReturn(List.of(ocupacoes));
    }

    @Test
    void devePermitirPeriodoSemNenhumaOcupacaoExistente() {
        mockOcupacoesExistentes();

        assertThatCode(() -> policy.assertPeriodoDisponivel(ativoId,
                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 15)))
                .doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarPeriodoQueSeSobrepoeAOcupacaoExistente() {
        mockOcupacoesExistentes(ocupacao(
                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 20), AtivoAlocacaoStatus.CONFIRMADA));

        assertThatThrownBy(() -> policy.assertPeriodoDisponivel(ativoId,
                LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 25)))
                .isInstanceOf(AtivoPeriodoConflitanteException.class);
    }

    @Test
    void deveRejeitarPeriodoTotalmenteContidoEmOcupacaoExistente() {
        mockOcupacoesExistentes(ocupacao(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), AtivoAlocacaoStatus.PENDENTE_APROVACAO));

        assertThatThrownBy(() -> policy.assertPeriodoDisponivel(ativoId,
                LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 15)))
                .isInstanceOf(AtivoPeriodoConflitanteException.class);
    }

    @Test
    void devePermitirPeriodosAdjacentesSemSobreposicao() {
        mockOcupacoesExistentes(ocupacao(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 10), AtivoAlocacaoStatus.CONFIRMADA));

        assertThatCode(() -> policy.assertPeriodoDisponivel(ativoId,
                LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 20)))
                .doesNotThrowAnyException();
    }

    @Test
    void deveIgnorarOcupacoesDevolvidasOuCanceladasParaCalculoDeConflito() {
        mockOcupacoesExistentes();

        assertThatCode(() -> policy.assertPeriodoDisponivel(ativoId,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 10)))
                .doesNotThrowAnyException();
    }
}
