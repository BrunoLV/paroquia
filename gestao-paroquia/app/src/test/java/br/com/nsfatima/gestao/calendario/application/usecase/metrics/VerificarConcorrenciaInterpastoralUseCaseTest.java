package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.ConcorrenciaInterpastoralResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificarConcorrenciaInterpastoralUseCaseTest {

    @Mock
    private EventoJpaRepository eventoJpaRepository;

    @InjectMocks
    private VerificarConcorrenciaInterpastoralUseCase useCase;

    @Test
    @DisplayName("Deve detectar concorrência interpastoral quando 2 pastorais agendam eventos similares em datas muito próximas")
    void deveDetectarConcorrenciaInterpastoralComSucesso() {
        Instant agora = Instant.now();
        UUID orgA = UUID.randomUUID();
        UUID orgB = UUID.randomUUID();

        EventoEntity evA = new EventoEntity();
        evA.setId(UUID.randomUUID());
        evA.setTitulo("Encontro de Casais Matriz");
        evA.setOrganizacaoResponsavelId(orgA);
        evA.setInicioUtc(agora);
        evA.setFimUtc(agora.plus(2, ChronoUnit.HOURS));
        evA.setCategoria(CategoriaEvento.PASTORAL);

        EventoEntity evB = new EventoEntity();
        evB.setId(UUID.randomUUID());
        evB.setTitulo("Encontro de Casais Capela");
        evB.setOrganizacaoResponsavelId(orgB);
        evB.setInicioUtc(agora.plus(1, ChronoUnit.DAYS));
        evB.setFimUtc(agora.plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS));
        evB.setCategoria(CategoriaEvento.PASTORAL);

        when(eventoJpaRepository.findAll()).thenReturn(List.of(evA, evB));

        ConcorrenciaInterpastoralResponse response = useCase.execute();

        assertNotNull(response);
        assertEquals(1, response.totalConflitosDetectados());
        assertFalse(response.conflitos().isEmpty());
        assertEquals("MEDIO", response.nivelRiscoConcorrencia());
    }
}
