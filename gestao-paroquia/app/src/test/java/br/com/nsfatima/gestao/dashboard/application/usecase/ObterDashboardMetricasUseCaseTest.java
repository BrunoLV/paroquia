package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricasDTO;
import br.com.nsfatima.gestao.dashboard.application.service.EscopoDashboardResolver;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObterDashboardMetricasUseCaseTest {

    private final EventoJpaRepository eventoRepository = mock(EventoJpaRepository.class);
    private final ProjetoEventoJpaRepository projetoRepository = mock(ProjetoEventoJpaRepository.class);
    private final AprovacaoJpaRepository aprovacaoRepository = mock(AprovacaoJpaRepository.class);
    private final ProjetoSolicitacaoAprovacaoJpaRepository projetoAprovacaoRepository =
            mock(ProjetoSolicitacaoAprovacaoJpaRepository.class);
    private final OrganizacaoJpaRepository organizacaoRepository = mock(OrganizacaoJpaRepository.class);
    private final LocalJpaRepository localRepository = mock(LocalJpaRepository.class);
    private final FielJpaRepository fielRepository = mock(FielJpaRepository.class);
    private final MembroGrupoJpaRepository membroRepository = mock(MembroGrupoJpaRepository.class);

    private final ObterDashboardMetricasUseCase useCase = new ObterDashboardMetricasUseCase(
            eventoRepository, projetoRepository, aprovacaoRepository, projetoAprovacaoRepository,
            organizacaoRepository, localRepository, fielRepository, membroRepository,
            new ProjetoAgregacaoDomainService(), new JanelaTemporalResolver(), new EscopoDashboardResolver());

    private static final UUID ORG_A = UUID.randomUUID();
    private static final UUID ORG_B = UUID.randomUUID();

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

    private void stubProjetoCounters(ProjetoEventoEntity p, long total, long realizados, long pendentes,
                                     long pendentesAprovacao, Optional<EventoEntity> proximoMarco) {
        when(eventoRepository.countByProjetoId(p.getId())).thenReturn(total);
        when(eventoRepository.countByProjetoIdAndFimUtcLessThan(eq(p.getId()), any())).thenReturn(realizados);
        when(eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(eq(p.getId()), any())).thenReturn(pendentes);
        when(eventoRepository.countByProjetoIdAndStatus(eq(p.getId()), any())).thenReturn(pendentesAprovacao);
        when(eventoRepository.findProximoMarco(eq(p.getId()), any())).thenReturn(proximoMarco);
        when(eventoRepository.findInvolvedOrganizationNames(p.getId())).thenReturn(List.of("Pastoral X"));
    }

    @Test
    void execute_semEscopo_deveAgregarTodasAsSecoesDoDashboard() {
        Instant agora = Instant.now();

        EventoEntity eventoA = evento(ORG_A, agora, agora.plus(1, ChronoUnit.HOURS), UUID.randomUUID());
        EventoEntity eventoB = evento(ORG_B, agora, agora.plus(1, ChronoUnit.HOURS), null);
        when(eventoRepository.findAllInPeriod(any(), any())).thenReturn(List.of(eventoA, eventoA, eventoB));

        // projeto1: ABERTO, prazo futuro e sem risco -> nao atrasado, sem eventos (percentual execucao zero)
        ProjetoEventoEntity projeto1 = projeto(ORG_A, ProjetoStatus.ABERTO, agora.minus(1, ChronoUnit.DAYS),
                agora.plus(30, ChronoUnit.DAYS));
        stubProjetoCounters(projeto1, 0, 0, 0, 0, Optional.empty());

        // projeto2: EM_ANDAMENTO com fim no passado -> atrasado pelo ramo fimUtc < agora
        ProjetoEventoEntity projeto2 = projeto(ORG_B, ProjetoStatus.EM_ANDAMENTO, agora.minus(10, ChronoUnit.DAYS),
                agora.minus(1, ChronoUnit.DAYS));
        stubProjetoCounters(projeto2, 4, 2, 1, 1, Optional.of(eventoA));

        // projeto3: EM_ANDAMENTO com fim futuro mas quase todo o prazo decorrido e pendencias -> risco
        ProjetoEventoEntity projeto3 = projeto(ORG_A, ProjetoStatus.EM_ANDAMENTO, agora.minus(9, ChronoUnit.DAYS),
                agora.plus(1, ChronoUnit.MINUTES));
        stubProjetoCounters(projeto3, 5, 5, 2, 0, Optional.empty());

        ProjetoEventoEntity projeto4 = projeto(ORG_A, ProjetoStatus.CONCLUIDO, agora.minus(60, ChronoUnit.DAYS),
                agora.minus(30, ChronoUnit.DAYS));

        when(projetoRepository.findAll()).thenReturn(List.of(projeto1, projeto2, projeto3, projeto4));

        OrganizacaoEntity orgA = new OrganizacaoEntity();
        orgA.setId(ORG_A);
        orgA.setNome("Comunidade A");
        OrganizacaoEntity orgB = new OrganizacaoEntity();
        orgB.setId(ORG_B);
        orgB.setNome("Comunidade B");
        when(organizacaoRepository.findAll()).thenReturn(List.of(orgA, orgB));

        LocalEntity local = new LocalEntity();
        local.setId(eventoA.getLocalId());
        local.setNome("Salão Paroquial");
        when(localRepository.findAll()).thenReturn(List.of(local));

        AprovacaoEntity aprovacaoPendente = new AprovacaoEntity();
        aprovacaoPendente.setId(UUID.randomUUID());
        aprovacaoPendente.setEventoId(eventoA.getId());
        when(aprovacaoRepository.findByStatusOrderByCriadoEmUtcAsc(AprovacaoStatus.PENDENTE))
                .thenReturn(List.of(aprovacaoPendente));

        ProjetoSolicitacaoAprovacaoEntity aprovacaoProjeto = new ProjetoSolicitacaoAprovacaoEntity();
        aprovacaoProjeto.setId(UUID.randomUUID());
        aprovacaoProjeto.setProjetoId(projeto1.getId());
        when(projetoAprovacaoRepository.findByStatus(AprovacaoStatus.PENDENTE)).thenReturn(List.of(aprovacaoProjeto));

        when(fielRepository.countByAtivoTrue()).thenReturn(10L);
        when(fielRepository.countByAtivoTrueAndCriadoEmUtcGreaterThanEqual(any())).thenReturn(3L);
        when(membroRepository.contarFieisDistintosEngajados(any())).thenReturn(4L);

        // Evento orfao com sugestao de projeto1 (mesma org e dentro da janela do projeto)
        EventoEntity orfaoComSugestao = evento(ORG_A, agora.plus(2, ChronoUnit.DAYS),
                agora.plus(3, ChronoUnit.DAYS), null);
        // Evento orfao sem sugestao (nenhuma organizacao/janela de projeto ativo compativel)
        EventoEntity orfaoSemSugestao = evento(ORG_B, agora.plus(2, ChronoUnit.DAYS),
                agora.plus(3, ChronoUnit.DAYS), null);
        when(eventoRepository.findOrfaosNoPeriodo(any(), any()))
                .thenReturn(List.of(orfaoComSugestao, orfaoSemSugestao));

        EventoActorContext actorContext = new EventoActorContext("atriz", "ADMIN", null, null, null, null);

        DashboardMetricasDTO resultado = useCase.execute(actorContext, PeriodoDashboard.SEMANA, null);

        assertThat(resultado.resumoGeral().totalEventosPeriodo()).isEqualTo(3);
        assertThat(resultado.resumoGeral().projetosAtivos()).isEqualTo(3);
        assertThat(resultado.resumoGeral().solicitacoesPendentes()).isEqualTo(2);
        assertThat(resultado.resumoGeral().totalFieisAtivos()).isEqualTo(10L);
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
    void execute_comEscopoDoAtor_deveFiltrarSomenteRecursosDaOrganizacao() {
        Instant agora = Instant.now();

        EventoEntity eventoOrgA = evento(ORG_A, agora, agora.plus(1, ChronoUnit.HOURS), null);
        EventoEntity eventoOrgB = evento(ORG_B, agora, agora.plus(1, ChronoUnit.HOURS), null);
        when(eventoRepository.findAllInPeriod(any(), any())).thenReturn(List.of(eventoOrgA, eventoOrgB));
        when(eventoRepository.findOrfaosNoPeriodo(any(), any())).thenReturn(List.of());

        ProjetoEventoEntity projetoOrgA = projeto(ORG_A, ProjetoStatus.ABERTO, agora.minus(1, ChronoUnit.DAYS),
                agora.plus(30, ChronoUnit.DAYS));
        ProjetoEventoEntity projetoOrgB = projeto(ORG_B, ProjetoStatus.ABERTO, agora.minus(1, ChronoUnit.DAYS),
                agora.plus(30, ChronoUnit.DAYS));
        stubProjetoCounters(projetoOrgA, 0, 0, 0, 0, Optional.empty());
        when(projetoRepository.findAll()).thenReturn(List.of(projetoOrgA, projetoOrgB));
        when(projetoRepository.findAllById(Set.of(projetoOrgA.getId(), projetoOrgB.getId())))
                .thenReturn(List.of(projetoOrgA, projetoOrgB));

        when(organizacaoRepository.findAll()).thenReturn(List.of());
        when(localRepository.findAll()).thenReturn(List.of());

        AprovacaoEntity aprovacaoOrgA = new AprovacaoEntity();
        aprovacaoOrgA.setId(UUID.randomUUID());
        aprovacaoOrgA.setEventoId(eventoOrgA.getId());
        AprovacaoEntity aprovacaoOrgB = new AprovacaoEntity();
        aprovacaoOrgB.setId(UUID.randomUUID());
        aprovacaoOrgB.setEventoId(eventoOrgB.getId());
        when(aprovacaoRepository.findByStatusOrderByCriadoEmUtcAsc(AprovacaoStatus.PENDENTE))
                .thenReturn(List.of(aprovacaoOrgA, aprovacaoOrgB));
        when(eventoRepository.findAllById(Set.of(eventoOrgA.getId(), eventoOrgB.getId())))
                .thenReturn(List.of(eventoOrgA, eventoOrgB));

        ProjetoSolicitacaoAprovacaoEntity aprovacaoProjetoOrgA = new ProjetoSolicitacaoAprovacaoEntity();
        aprovacaoProjetoOrgA.setId(UUID.randomUUID());
        aprovacaoProjetoOrgA.setProjetoId(projetoOrgA.getId());
        ProjetoSolicitacaoAprovacaoEntity aprovacaoProjetoOrgB = new ProjetoSolicitacaoAprovacaoEntity();
        aprovacaoProjetoOrgB.setId(UUID.randomUUID());
        aprovacaoProjetoOrgB.setProjetoId(projetoOrgB.getId());
        when(projetoAprovacaoRepository.findByStatus(AprovacaoStatus.PENDENTE))
                .thenReturn(List.of(aprovacaoProjetoOrgA, aprovacaoProjetoOrgB));

        when(fielRepository.countByAtivoTrue()).thenReturn(5L);
        when(fielRepository.countByAtivoTrueAndCriadoEmUtcGreaterThanEqual(any())).thenReturn(1L);
        when(membroRepository.contarFieisDistintosEngajados(any())).thenReturn(2L);

        EventoActorContext actorContext = new EventoActorContext("atriz", "COORDENADOR", "PASTORAL", ORG_A, null, null);

        DashboardMetricasDTO resultado = useCase.execute(actorContext, PeriodoDashboard.HOJE, null);

        assertThat(resultado.resumoGeral().totalEventosPeriodo()).isEqualTo(1);
        assertThat(resultado.resumoGeral().solicitacoesPendentes()).isEqualTo(2);
        assertThat(resultado.resumoGeral().projetosAtivos()).isEqualTo(1);
        assertThat(resultado.eventosPorOrganizacao()).hasSize(1);
    }
}
