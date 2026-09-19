package br.com.nsfatima.gestao.calendario.application.usecase.metricas;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metricas.TaxaConclusaoProjetosResponse;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularTaxaProjetosUseCaseTest {

    @Mock
    private ProjetoEventoJpaRepository projetoEventoJpaRepository;

    @InjectMocks
    private CalcularTaxaProjetosUseCase useCase;

    @Test
    @DisplayName("Deve calcular taxa de conclusão e abandono de projetos com métricas estatísticas")
    void deveCalcularTaxaProjetosComSucesso() {
        when(projetoEventoJpaRepository.findAll()).thenReturn(List.of());

        TaxaConclusaoProjetosResponse response = useCase.execute();

        assertNotNull(response);
        assertNotNull(response.totalProjetos());
        assertNotNull(response.taxaConclusaoPercentual());
        assertNotNull(response.taxaAbandonoPercentual());
        assertNotNull(response.avaliacaoExecutiva());
    }
}
