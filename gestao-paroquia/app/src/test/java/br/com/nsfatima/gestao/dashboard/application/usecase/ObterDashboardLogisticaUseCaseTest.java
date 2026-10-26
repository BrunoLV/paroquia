package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardLogisticaDTO;
import br.com.nsfatima.gestao.dashboard.application.service.EscopoDashboardResolver;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ReservaLocalIntervalo;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.repository.PainelConsultaJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObterDashboardLogisticaUseCaseTest {

    private static final ZoneId BRT = ZoneId.of("America/Sao_Paulo");
    private static final Instant AGORA = Instant.parse("2026-06-10T15:00:00Z");
    private static final UUID ORG = UUID.randomUUID();

    private final EventoJpaRepository eventoRepository = mock(EventoJpaRepository.class);
    private final PainelConsultaJpaRepository painelRepository = mock(PainelConsultaJpaRepository.class);
    private final LocalJpaRepository localRepository = mock(LocalJpaRepository.class);
    private final OrganizacaoJpaRepository organizacaoRepository = mock(OrganizacaoJpaRepository.class);

    private final ObterDashboardLogisticaUseCase useCase = new ObterDashboardLogisticaUseCase(
            eventoRepository, painelRepository, localRepository, organizacaoRepository,
            new JanelaTemporalResolver(BRT), new EscopoDashboardResolver(),
            new RelogioNegocio(Clock.fixed(AGORA, ZoneOffset.UTC), BRT));

    private final EventoActorContext admin = new EventoActorContext("admin", "ADMIN", null, null, null, null);

    private LocalEntity local(String nome) {
        LocalEntity l = new LocalEntity();
        l.setId(UUID.randomUUID());
        l.setNome(nome);
        return l;
    }

    @Test
    @DisplayName("Ocupacao vem das reservas de LOCAL, une sobreposicoes e recorta na janela do dia")
    void ocupacaoDerivadaDeReservasComUniaoDeSobreposicoes() {
        LocalEntity salao = local("Salao");
        LocalEntity capela = local("Capela");
        when(localRepository.findByAtivoTrue()).thenReturn(List.of(salao, capela));
        // dia de Brasilia: 2026-06-10T03:00Z a 2026-06-11T03:00Z
        when(painelRepository.reservasDeLocalNaJanela(eq(Instant.parse("2026-06-10T03:00:00Z")),
                eq(Instant.parse("2026-06-11T03:00:00Z")), isNull())).thenReturn(List.of(
                new ReservaLocalIntervalo(salao.getId(), Instant.parse("2026-06-10T12:00:00Z"),
                        Instant.parse("2026-06-10T14:00:00Z")),
                new ReservaLocalIntervalo(salao.getId(), Instant.parse("2026-06-10T13:00:00Z"),
                        Instant.parse("2026-06-10T15:00:00Z"))));

        DashboardLogisticaDTO resultado = useCase.execute(admin, PeriodoDashboard.HOJE, null);

        // 180 min unidos / (2 locais * 14h * 60) = 10,7%
        assertThat(resultado.ocupacaoAmbientes().taxaDiaPercentual()).isEqualByComparingTo("10.7");
    }

    @Test
    @DisplayName("Com escopo, a ocupacao considera so reservas da organizacao responsavel, sobre todos os ambientes")
    void escopoFiltraReservasPelaOrganizacaoResponsavel() {
        LocalEntity salao = local("Salao");
        when(localRepository.findByAtivoTrue()).thenReturn(List.of(salao));
        when(painelRepository.reservasDeLocalNaJanela(any(), any(), eq(ORG))).thenReturn(List.of());

        useCase.execute(new EventoActorContext("coord", "COORDENADOR", "PASTORAL", ORG, null, null),
                PeriodoDashboard.HOJE, null);

        verify(painelRepository, atLeastOnce()).reservasDeLocalNaJanela(any(), any(), eq(ORG));
    }

    @Test
    @DisplayName("Pendencias de homologacao respeitam o escopo organizacional")
    void pendenciasRespeitamOEscopo() {
        when(localRepository.findByAtivoTrue()).thenReturn(List.of());
        when(painelRepository.contarAprovacoesDeEventoPorTipo(AprovacaoStatus.PENDENTE, ORG))
                .thenReturn(List.<Object[]>of(new Object[] {"CRIACAO_EVENTO", 2L}));
        when(painelRepository.contarSolicitacoesDeProjetoPorTipo(AprovacaoStatus.PENDENTE, ORG))
                .thenReturn(List.<Object[]>of(new Object[] {"CRIACAO_EVENTO", 1L}, new Object[] {"CRIACAO", 1L}));

        DashboardLogisticaDTO resultado = useCase.execute(
                new EventoActorContext("coord", "COORDENADOR", "PASTORAL", ORG, null, null),
                PeriodoDashboard.SEMANA, null);

        assertThat(resultado.pendenciasHomologacao()).extracting("tipo", "quantidade")
                .containsExactlyInAnyOrder(
                        tuple("CRIACAO_EVENTO", 3L),
                        tuple("CRIACAO", 1L));
    }
}
