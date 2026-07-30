package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SazonalidadeLiturgicaResponse;
import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularSazonalidadeLiturgicaUseCaseTest {

    @Mock
    private CicloLiturgicoRepository cicloLiturgicoRepository;

    @InjectMocks
    private CalcularSazonalidadeLiturgicaUseCase useCase;

    @Test
    @DisplayName("Deve calcular sazonalidade litúrgica e projeção preditiva com sucesso")
    void deveCalcularSazonalidadeLiturgicaComSucesso() {
        CicloLiturgicoEntity quaresma = new CicloLiturgicoEntity();
        quaresma.setNome("Quaresma");
        quaresma.setTipoCiclo(TipoCicloLiturgico.QUARESMA);
        quaresma.setFatorDemanda(2.5);

        when(cicloLiturgicoRepository.findAll()).thenReturn(List.of(quaresma));

        List<SazonalidadeLiturgicaResponse> result = useCase.execute();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Quaresma", result.get(0).cicloNome());
        assertEquals(2.5, result.get(0).fatorDemanda());
        assertEquals("PICO_OPERACIONAL", result.get(0).nivelDemanda());
    }
}
