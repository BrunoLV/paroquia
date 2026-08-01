package br.com.nsfatima.gestao.patrimonio.application.usecase;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAlocacoesPorEventoUseCaseTest {

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    private ListAlocacoesPorEventoUseCase useCase;
    private UUID eventoId;

    @BeforeEach
    void setUp() {
        useCase = new ListAlocacoesPorEventoUseCase(alocacaoRepository, ativoRepository);
        eventoId = UUID.randomUUID();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaAtivosAlocados() {
        when(alocacaoRepository.findByEventoIdOrderByDataInicioDesc(eventoId)).thenReturn(List.of());

        assertThat(useCase.execute(eventoId)).isEmpty();
    }

    @Test
    void deveRetornarAtivosAlocadosComNomeDenormalizado() {
        UUID ativoId = UUID.randomUUID();
        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativoId);
        ocupacao.setEventoId(eventoId);
        ocupacao.setTipo(AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        ocupacao.setDataInicio(LocalDate.now());
        ocupacao.setDataFim(LocalDate.now().plusDays(1));
        ocupacao.setStatus(AtivoAlocacaoStatus.CONFIRMADA);

        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Caixa de Som");
        ativo.setNumeroTombamento("PAT-2026-0002");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.ALOCADO);
        ativo.setOrganizacaoId(UUID.randomUUID());

        when(alocacaoRepository.findByEventoIdOrderByDataInicioDesc(eventoId)).thenReturn(List.of(ocupacao));
        when(ativoRepository.findAllById(List.of(ativoId))).thenReturn(List.of(ativo));

        var responses = useCase.execute(eventoId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).ativoNome()).isEqualTo("Caixa de Som");
        assertThat(responses.get(0).eventoId()).isEqualTo(eventoId);
    }
}
