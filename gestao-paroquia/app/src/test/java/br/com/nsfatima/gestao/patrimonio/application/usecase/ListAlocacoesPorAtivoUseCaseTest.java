package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAlocacoesPorAtivoUseCaseTest {

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    private ListAlocacoesPorAtivoUseCase useCase;
    private UUID ativoId;

    @BeforeEach
    void setUp() {
        useCase = new ListAlocacoesPorAtivoUseCase(alocacaoRepository, ativoRepository);
        ativoId = UUID.randomUUID();
    }

    private AtivoPatrimonialEntity ativo() {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Projetor");
        ativo.setNumeroTombamento("PAT-2026-0001");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.DISPONIVEL);
        ativo.setOrganizacaoId(UUID.randomUUID());
        return ativo;
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaOcupacoes() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo()));
        when(alocacaoRepository.findByAtivoIdOrderByDataInicioDesc(ativoId)).thenReturn(List.of());

        assertThat(useCase.execute(ativoId)).isEmpty();
    }

    @Test
    void deveRetornarHistoricoComEventosEManutencoes() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo()));

        AtivoAlocacaoEntity reservaFutura = new AtivoAlocacaoEntity();
        reservaFutura.setId(UUID.randomUUID());
        reservaFutura.setAtivoId(ativoId);
        reservaFutura.setTipo(AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        reservaFutura.setDataInicio(LocalDate.now().plusDays(1));
        reservaFutura.setDataFim(LocalDate.now().plusDays(3));
        reservaFutura.setStatus(AtivoAlocacaoStatus.CONFIRMADA);

        AtivoAlocacaoEntity manutencaoDevolvida = new AtivoAlocacaoEntity();
        manutencaoDevolvida.setId(UUID.randomUUID());
        manutencaoDevolvida.setAtivoId(ativoId);
        manutencaoDevolvida.setTipo(AtivoAlocacaoTipo.MANUTENCAO);
        manutencaoDevolvida.setDataInicio(LocalDate.now().minusDays(10));
        manutencaoDevolvida.setDataFim(LocalDate.now().minusDays(5));
        manutencaoDevolvida.setStatus(AtivoAlocacaoStatus.DEVOLVIDA);

        when(alocacaoRepository.findByAtivoIdOrderByDataInicioDesc(ativoId))
                .thenReturn(List.of(reservaFutura, manutencaoDevolvida));

        var responses = useCase.execute(ativoId);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).ativoNome()).isEqualTo("Projetor");
        assertThat(responses.get(0).tipo()).isEqualTo(AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        assertThat(responses.get(1).tipo()).isEqualTo(AtivoAlocacaoTipo.MANUTENCAO);
    }

    @Test
    void deveLancarExcecaoQuandoAtivoNaoExiste() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ativoId))
                .isInstanceOf(AtivoPatrimonialNotFoundException.class);
    }
}
