package br.com.nsfatima.gestao.aprovacao.application.service;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import br.com.nsfatima.gestao.calendario.application.service.DetectorConflitoEventoService;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AprovacaoRecalculoConflitoServiceTest {

    @Mock
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Mock
    private DetectorConflitoEventoService detectorConflitoEventoService;

    @Mock
    private AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve recalcular e atualizar conflitosJson para todas as solicitações pendentes concorrentes")
    void deveRecalcularConflitosDasSolicitacoesPendentes() {
        AprovacaoRecalculoConflitoService service = new AprovacaoRecalculoConflitoService(
                aprovacaoJpaRepository,
                detectorConflitoEventoService,
                aprovacaoAcaoPayloadMapper,
                objectMapper
        );

        UUID recemAprovadaId = UUID.randomUUID();

        UUID pendenteId = UUID.randomUUID();
        AprovacaoEntity pendente = new AprovacaoEntity();
        pendente.setId(pendenteId);
        pendente.setStatus(AprovacaoStatus.PENDENTE);
        pendente.setActionPayloadJson("{\"titulo\": \"Reunião de Jovens\"}");

        when(aprovacaoJpaRepository.findByStatusOrderByCriadoEmUtcAsc(AprovacaoStatus.PENDENTE))
                .thenReturn(List.of(pendente));

        AprovacaoAcaoPayload payload = new AprovacaoAcaoPayload(
                "idemp-pendente",
                null,
                "Reunião de Jovens",
                "Descricao",
                null,
                UUID.randomUUID(),
                null,
                Instant.parse("2026-10-15T14:00:00Z"),
                Instant.parse("2026-10-15T16:00:00Z"),
                null,
                null,
                null,
                null,
                List.of(),
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                null,
                null,
                null,
                List.of(),
                null,
                TipoHorarioEvento.PERSONALIZADO,
                FormatoEvento.PRESENCIAL
        );

        when(aprovacaoAcaoPayloadMapper.toPayload(pendente.getActionPayloadJson())).thenReturn(payload);

        ConflitoEventoItemDto novoConflito = new ConflitoEventoItemDto(
                "LOCAL",
                payload.localId(),
                "Salão Paroquial",
                UUID.randomUUID(),
                "Missa Solene",
                payload.inicio(),
                payload.fim(),
                "Padre Pedro",
                1,
                2,
                "Espaço reservado pelo evento recém-aprovado"
        );

        when(detectorConflitoEventoService.detectarConflitos(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(novoConflito));

        service.recalcularConflitosPendentes(recemAprovadaId);

        assertThat(pendente.getConflitosJson()).isNotNull();
        assertThat(pendente.getConflitosJson()).contains("LOCAL");
        assertThat(pendente.getConflitosJson()).contains("Salão Paroquial");
        verify(aprovacaoJpaRepository).save(pendente);
    }
}
