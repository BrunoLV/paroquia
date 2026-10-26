package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricasDTO;
import br.com.nsfatima.gestao.dashboard.application.service.EscopoDashboardResolver;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ContagemPorProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.EstatisticaEventosProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.repository.PainelConsultaJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObterDashboardMetricasUseCaseTest {

    private static final Instant AGORA = Instant.parse("2026-06-10T15:00:00Z");
    private static final UUID ORG_A = UUID.randomUUID();
    private static final UUID ORG_B = UUID.randomUUID();

    private final EventoJpaRepository eventoRepository = mock(EventoJpaRepository.class);
    private final PainelConsultaJpaRepository painelRepository = mock(PainelConsultaJpaRepository.class);
    private final OrganizacaoJpaRepository organizacaoRepository = mock(OrganizacaoJpaRepository.class);
    private final LocalJpaRepository localRepository = mock(LocalJpaRepository.class);
    private final FielJpaRepository fielRepository = mock(FielJpaRepository.class);
    private final MembroGrupoJpaRepository membroRepository = mock(MembroGrupoJpaRepository.class);

    private final ObterDashboardMetricasUseCase useCase = new ObterDashboardMetricasUseCase(
            eventoRepository, painelRepository, organizacaoRepository, localRepository, fielRepository,
            membroRepository, new ProjetoAgregacaoDomainService(),
            new JanelaTemporalResolver(ZoneId.of("America/Sao_Paulo")), new EscopoDashboardResolver(),
            new RelogioNegocio(Clock.fixed(AGORA, ZoneOffset.UTC), ZoneId.of("America/Sao_Paulo")));

    private EventoEntity evento(UUID org, Instant inicio, Instant fim, UUID localId) {
        EventoEntity e = new EventoEntity();
        e.setId(UUID.randomUUID());
        e.setTitulo("Evento " + org);
        e.setOrganizacaoResponsavelId(org);
        e.setInicioUtc(inicio);
        e.setFimUtc(fim);
        e.setLocalId(localId);
        return e;
    }

    private ProjetoEventoEntity projeto(UUID org, ProjetoStatus status, Instant inicio, Instant fim) {
        ProjetoEventoEntity p = new ProjetoEventoEntity();
        p.setId(UUID.randomUUID());
        p.setNome("Projeto " + status);
        p.setStatus(status);
        p.setOrganizacaoResponsavelId(org);
        p.setInicioUtc(inicio);
        p.setFimUtc(fim);
        return p;
    }

    private OrganizacaoEntity organizacao(UUID id, String nome) {
        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(id);
        org.setNome(nome);
        return org;
    }

    @Test
    void execute_semEscopo_deveAgregarTodasAsSecoesDoDashboard() {
        EventoEntity eventoA = evento(ORG_A, AGORA, AGORA.plus(1, ChronoUnit.HOURS), UUID.randomUUID());
        EventoEntity eventoB = evento(ORG_B, AGORA, AGORA.plus(1, ChronoUnit.HOURS), null);
        when(eventoRepository.findAllInPeriod(any(), any())).thenReturn(List.of(eventoA, eventoA, eventoB));

        // projeto1: ABERTO, prazo futuro, sem eventos pendentes -> nao atrasado
        ProjetoEventoEntity projeto1 = projeto(ORG_A, ProjetoStatus.ABERTO, AGORA.minus(1, ChronoUnit.DAYS),
                AGORA.plus(30, ChronoUnit.DAYS));
        // projeto2: EM_ANDAMENTO com fim no passado -> atrasado
        ProjetoEventoEntity projeto2 = projeto(ORG_B, ProjetoStatus.EM_ANDAMENTO, AGORA.minus(10, ChronoUnit.DAYS),
                AGORA.minus(1, ChronoUnit.DAYS));
        // projeto3: quase todo o prazo decorrido e com pendencias -> risco
        ProjetoEventoEntity projeto3 = projeto(ORG_A, ProjetoStatus.EM_ANDAMENTO, AGORA.minus(9, ChronoUnit.DAYS),
                AGORA.plus(1, ChronoUnit.MINUTES));
        when(painelRepository.findProjetos(any(), isNull())).thenReturn(List.of(projeto1, projeto2, projeto3));
        when(painelRepository.contarProjetosPorStatus(isNull())).thenReturn(List.of(
                new Object[] {ProjetoStatus.ABERTO, 1L},
                new Object[] {ProjetoStatus.EM_ANDAMENTO, 2L},
                new Object[] {ProjetoStatus.CONCLUIDO, 1L}));
        when(painelRepository.estatisticasEventosPorProjeto(any(), eq(AGORA))).thenReturn(List.of(
                new EstatisticaEventosProjeto(projeto2.getId(), 4L, 2L, 1L),
                new EstatisticaEventosProjeto(projeto3.getId(), 5L, 5L, 2L)));
        when(painelRepository.eventosFuturosDosProjetos(any(), eq(AGORA))).thenReturn(List.of(eventoA));
        eventoA.setProjetoId(projeto2.getId());
        when(painelRepository.contarAprovacoesPorProjeto(any(), eq(AprovacaoStatus.PENDENTE)))
                .thenReturn(List.of(new ContagemPorProjeto(projeto2.getId(), 1L)));
        when(painelRepository.nomesOrganizacoesEnvolvidasPorProjeto(any()))
                .thenReturn(List.<Object[]>of(new Object[] {projeto2.getId(), "Pastoral X"}));
        when(organizacaoRepository.findAllById(any()))
                .thenReturn(List.of(organizacao(ORG_A, "Comunidade A"), organizacao(ORG_B, "Comunidade B")));
        LocalEntity local = new LocalEntity();
        local.setId(eventoA.getLocalId());
        local.setNome("Salão Paroquial");
        when(localRepository.findAllById(any())).thenReturn(List.of(local));

        when(painelRepository.contarAprovacoesDeEvento(AprovacaoStatus.PENDENTE, null)).thenReturn(1L);
        when(painelRepository.contarSolicitacoesDeProjeto(AprovacaoStatus.PENDENTE, null)).thenReturn(1L);
        when(fielRepository.countByAtivoTrue()).thenReturn(10L);
        when(fielRepository.countByAtivoTrueAndCriadoEmUtcGreaterThanEqual(any())).thenReturn(3L);
        when(membroRepository.contarFieisDistintosEngajados(any())).thenReturn(4L);

        EventoEntity orfaoComSugestao = evento(ORG_A, AGORA.plus(2, ChronoUnit.DAYS),
                AGORA.plus(3, ChronoUnit.DAYS), null);
        EventoEntity orfaoSemSugestao = evento(ORG_B, AGORA.plus(2, ChronoUnit.DAYS),
                AGORA.plus(3, ChronoUnit.DAYS), null);
        when(eventoRepository.findOrfaosNoPeriodo(any(), any()))
                .thenReturn(List.of(orfaoComSugestao, orfaoSemSugestao));

        DashboardMetricasDTO resultado = useCase.execute(
                new EventoActorContext("atriz", "ADMIN", null, null, null, null), PeriodoDashboard.SEMANA, null);

        assertThat(resultado.resumoGeral().totalEventosPeriodo()).isEqualTo(3);
        assertThat(resultado.resumoGeral().projetosAtivos()).isEqualTo(3);
        assertThat(resultado.resumoGeral().solicitacoesPendentes()).isEqualTo(2);
        assertThat(resultado.resumoGeral().totalFieisAtivos()).isEqualTo(10L);
        assertThat(resultado.resumoGeral().novosFieisMes()).isEqualTo(3L);
        assertThat(resultado.resumoGeral().fieisEngajadosPastoral()).isEqualTo(4L);
        assertThat(resultado.resumoGeral().fieisSemVinculoPastoral()).isEqualTo(6L);

        assertThat(resultado.projetosPorStatus().planejamento()).isEqualTo(1);
        assertThat(resultado.projetosPorStatus().emExecucao()).isEqualTo(2);
        assertThat(resultado.projetosPorStatus().concluidos()).isEqualTo(1);
        assertThat(resultado.projetosPorStatus().atrasados()).isEqualTo(2);

        assertThat(resultado.projetosAtivosDetalhes()).hasSize(3);
        assertThat(resultado.projetosAtivosDetalhes())
                .anySatisfy(d -> assertThat(d.percentualExecucao()).isEqualByComparingTo("0.0"))
                .anySatisfy(d -> assertThat(d.proximoMarco()).isNotNull())
                .anySatisfy(d -> assertThat(d.proximoMarco()).isNull());

        assertThat(resultado.eventosOrfaos()).hasSize(2);
        assertThat(resultado.eventosOrfaos())
                .anySatisfy(o -> assertThat(o.projetoSugeridoId()).isEqualTo(projeto1.getId()))
                .anySatisfy(o -> assertThat(o.projetoSugeridoId()).isNull());

        assertThat(resultado.eventosPorOrganizacao()).hasSize(2);
        assertThat(resultado.eventosPorOrganizacao().get(0).organizacaoNome()).isEqualTo("Comunidade A");
        assertThat(resultado.eventosPorOrganizacao().get(0).quantidadeEventos()).isEqualTo(2L);
    }

    @Test
    @DisplayName("pendentesAprovacao do projeto conta aprovacoes PENDENTE, nao eventos em RASCUNHO")
    void pendentesAprovacaoContaAprovacoesPendentes() {
        ProjetoEventoEntity projeto = projeto(ORG_A, ProjetoStatus.ABERTO, AGORA.minus(1, ChronoUnit.DAYS),
                AGORA.plus(30, ChronoUnit.DAYS));
        when(eventoRepository.findAllInPeriod(any(), any())).thenReturn(List.of());
        when(painelRepository.findProjetos(any(), isNull())).thenReturn(List.of(projeto));
        when(painelRepository.contarAprovacoesPorProjeto(any(), eq(AprovacaoStatus.PENDENTE)))
                .thenReturn(List.of(new ContagemPorProjeto(projeto.getId(), 3L)));

        DashboardMetricasDTO resultado = useCase.execute(
                new EventoActorContext("atriz", "ADMIN", null, null, null, null), PeriodoDashboard.HOJE, null);

        assertThat(resultado.projetosAtivosDetalhes()).singleElement()
                .satisfies(d -> assertThat(d.eventosPendentesAprovacao()).isEqualTo(3));
        verify(eventoRepository, never()).countByProjetoIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("Indicadores de todos os projetos saem de consultas agregadas: sem findAll nem N+1")
    void naoFazConsultaPorProjetoNemFindAll() {
        List<ProjetoEventoEntity> projetos = List.of(
                projeto(ORG_A, ProjetoStatus.ABERTO, AGORA.minus(1, ChronoUnit.DAYS), AGORA.plus(30, ChronoUnit.DAYS)),
                projeto(ORG_A, ProjetoStatus.EM_ANDAMENTO, AGORA.minus(1, ChronoUnit.DAYS),
                        AGORA.plus(30, ChronoUnit.DAYS)),
                projeto(ORG_A, ProjetoStatus.EM_ANDAMENTO, AGORA.minus(1, ChronoUnit.DAYS),
                        AGORA.plus(30, ChronoUnit.DAYS)));
        when(painelRepository.findProjetos(any(), isNull())).thenReturn(projetos);
        when(eventoRepository.findAllInPeriod(any(), any())).thenReturn(List.of());

        useCase.execute(new EventoActorContext("atriz", "ADMIN", null, null, null, null),
                PeriodoDashboard.SEMANA, null);

        verify(painelRepository, times(1)).estatisticasEventosPorProjeto(any(), any());
        verify(painelRepository, times(1)).eventosFuturosDosProjetos(any(), any());
        verify(painelRepository, times(1)).nomesOrganizacoesEnvolvidasPorProjeto(any());
        verify(eventoRepository, never()).countByProjetoId(any());
        verify(eventoRepository, never()).findProximoMarco(any(), any());
        verify(organizacaoRepository, never()).findAll();
        verify(localRepository, never()).findAll();
    }

    @Test
    void execute_comEscopoDoAtor_deveAplicarOEscopoAFieisEAprovacoes() {
        EventoEntity eventoOrgA = evento(ORG_A, AGORA, AGORA.plus(1, ChronoUnit.HOURS), null);
        EventoEntity eventoOrgB = evento(ORG_B, AGORA, AGORA.plus(1, ChronoUnit.HOURS), null);
        when(eventoRepository.findAllInPeriod(any(), any())).thenReturn(List.of(eventoOrgA, eventoOrgB));
        when(painelRepository.findProjetos(any(), eq(ORG_A))).thenReturn(List.of(
                projeto(ORG_A, ProjetoStatus.ABERTO, AGORA.minus(1, ChronoUnit.DAYS),
                        AGORA.plus(30, ChronoUnit.DAYS))));
        when(painelRepository.contarAprovacoesDeEvento(AprovacaoStatus.PENDENTE, ORG_A)).thenReturn(1L);
        when(painelRepository.contarSolicitacoesDeProjeto(AprovacaoStatus.PENDENTE, ORG_A)).thenReturn(1L);
        when(painelRepository.contarFieisAtivosDaOrganizacao(ORG_A)).thenReturn(8L);
        when(painelRepository.contarFieisNovosDaOrganizacao(eq(ORG_A), eq(Instant.parse("2026-06-01T03:00:00Z"))))
                .thenReturn(2L);
        when(painelRepository.contarFieisEngajadosDaOrganizacao(eq(ORG_A), any())).thenReturn(5L);
        when(organizacaoRepository.findAllById(any())).thenReturn(List.of(organizacao(ORG_A, "Comunidade A")));

        EventoActorContext ator = new EventoActorContext("atriz", "COORDENADOR", "PASTORAL", ORG_A, null, null);
        DashboardMetricasDTO resultado = useCase.execute(ator, PeriodoDashboard.HOJE, null);

        assertThat(resultado.resumoGeral().totalEventosPeriodo()).isEqualTo(1);
        assertThat(resultado.resumoGeral().solicitacoesPendentes()).isEqualTo(2);
        assertThat(resultado.resumoGeral().projetosAtivos()).isEqualTo(1);
        assertThat(resultado.resumoGeral().totalFieisAtivos()).isEqualTo(8L);
        assertThat(resultado.resumoGeral().novosFieisMes()).isEqualTo(2L);
        assertThat(resultado.resumoGeral().fieisEngajadosPastoral()).isEqualTo(5L);
        assertThat(resultado.resumoGeral().fieisSemVinculoPastoral()).isEqualTo(3L);
        assertThat(resultado.eventosPorOrganizacao()).hasSize(1);
        verify(fielRepository, never()).countByAtivoTrue();
        verify(membroRepository, never()).contarFieisDistintosEngajados(any());
    }

    @Test
    @DisplayName("Novos fieis do mes contam desde a meia-noite de Brasilia do dia 1, nao de UTC")
    void novosFieisDoMesUsamOInicioDoMesEmBrasilia() {
        when(eventoRepository.findAllInPeriod(any(), any())).thenReturn(List.of());
        when(painelRepository.findProjetos(any(), isNull())).thenReturn(List.of());

        useCase.execute(new EventoActorContext("atriz", "ADMIN", null, null, null, null), PeriodoDashboard.MES, null);

        verify(fielRepository).countByAtivoTrueAndCriadoEmUtcGreaterThanEqual(Instant.parse("2026-06-01T03:00:00Z"));
    }
}
