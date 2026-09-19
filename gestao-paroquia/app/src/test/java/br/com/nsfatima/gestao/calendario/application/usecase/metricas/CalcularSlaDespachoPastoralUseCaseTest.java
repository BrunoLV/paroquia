package br.com.nsfatima.gestao.calendario.application.usecase.metricas;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metricas.SlaDespachoPastoralResponse;

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
class CalcularSlaDespachoPastoralUseCaseTest {

    @Mock
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @InjectMocks
    private CalcularSlaDespachoPastoralUseCase useCase;

    @Test
    @DisplayName("Deve calcular SLA de Despacho Pastoral e tempo médio de aprovação com sucesso")
    void deveCalcularSlaDespachoPastoralComSucesso() {
        Instant agora = Instant.now();
        Instant duasHorasAtras = agora.minus(2, ChronoUnit.HOURS);

        AprovacaoEntity ap1 = new AprovacaoEntity();
        ap1.setId(UUID.randomUUID());
        ap1.setStatus(AprovacaoStatus.APROVADA);
        ap1.setCriadoEmUtc(duasHorasAtras);
        ap1.setDecididoEmUtc(agora);

        when(aprovacaoJpaRepository.findAll()).thenReturn(List.of(ap1));

        SlaDespachoPastoralResponse response = useCase.execute();

        assertNotNull(response);
        assertEquals(1L, response.totalSolicitacoes());
        assertEquals(1L, response.solicitacoesAprovadas());
        assertEquals(BigDecimal.valueOf(2.0), response.tempoMedioAprovacaoHoras());
        assertEquals("EXCELENTE", response.statusSla());
    }
}
