package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.CoeficienteImpactoSocialResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularImpactoSocialUseCaseTest {

    @Mock
    private EventoJpaRepository eventoJpaRepository;

    @InjectMocks
    private CalcularImpactoSocialUseCase useCase;

    @Test
    @DisplayName("Deve calcular coeficiente de impacto social (Pastoral em Saída) com sucesso")
    void deveCalcularImpactoSocialComSucesso() {
        EventoEntity ev1 = new EventoEntity();
        ev1.setId(UUID.randomUUID());
        ev1.setTitulo("Ação Social Bazar Solidário");
        ev1.setCategoria("SOCIAL");

        EventoEntity ev2 = new EventoEntity();
        ev2.setId(UUID.randomUUID());
        ev2.setTitulo("Reunião da Secretaria");
        ev2.setCategoria("REUNIAO_INTERNA");

        when(eventoJpaRepository.findAll()).thenReturn(List.of(ev1, ev2));

        CoeficienteImpactoSocialResponse response = useCase.execute();

        assertNotNull(response);
        assertEquals(2L, response.totalEventos());
        assertEquals(1L, response.eventosSociaisCaridade());
        assertEquals(1L, response.reunioesInternas());
        assertEquals(50.0, response.coeficienteImpactoSocialPercentual());
        assertEquals("EQUILIBRADA", response.diagnosticoPastoral());
    }
}
