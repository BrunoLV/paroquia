package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.OciosidadePatrimonialResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularOciosidadePatrimonialUseCaseTest {

    @Mock
    private LocalJpaRepository localJpaRepository;

    @Mock
    private EventoJpaRepository eventoJpaRepository;

    @InjectMocks
    private CalcularOciosidadePatrimonialUseCase useCase;

    @Test
    @DisplayName("Deve calcular o Índice de Ociosidade Patrimonial das salas e espaços da paróquia")
    void deveCalcularOciosidadePatrimonialComSucesso() {
        LocalEntity local1 = new LocalEntity();
        local1.setId(UUID.randomUUID());
        local1.setNome("Sala 01 Catequese");

        Instant inicio = Instant.now();
        Instant fim = inicio.plus(4, ChronoUnit.HOURS);

        EventoEntity ev1 = new EventoEntity();
        ev1.setId(UUID.randomUUID());
        ev1.setTitulo("Encontro de Catequistas");
        ev1.setInicioUtc(inicio);
        ev1.setFimUtc(fim);

        when(localJpaRepository.findAll()).thenReturn(List.of(local1));
        when(eventoJpaRepository.findAll()).thenReturn(List.of(ev1));

        OciosidadePatrimonialResponse response = useCase.execute();

        assertNotNull(response);
        assertEquals(1L, response.totalLocais());
        assertEquals(BigDecimal.valueOf(40.0), response.totalHorasUteisSemana());
        assertEquals(BigDecimal.valueOf(4.0), response.horasOcupadasSemana());
        assertEquals(BigDecimal.valueOf(36.0), response.horasOciosasSemana());
        assertEquals(BigDecimal.valueOf(90.0), response.indiceOciosidadePercentual());
    }
}
