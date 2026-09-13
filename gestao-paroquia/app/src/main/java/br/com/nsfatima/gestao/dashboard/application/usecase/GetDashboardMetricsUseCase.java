package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricsDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DetalheProjetoDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DistribuicaoEventosDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.EventoOrfaoDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.EventoSemanalDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.ProjetosPorStatusDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.ProximoMarcoDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.ResumoGeralDTO;
import br.com.nsfatima.gestao.dashboard.application.service.EscopoDashboardResolver;
import br.com.nsfatima.gestao.dashboard.domain.model.JanelaTemporal;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agrega, em uma única leitura, as métricas das seções "crítica" e "projetos" do dashboard
 * gerencial. Não existe tabela de métricas persistida: tudo é calculado sob demanda a partir dos
 * contextos de calendário, projeto, aprovação, fiel e membro. As métricas de volume (eventos do
 * período, distribuição por organização, eventos órfãos) respeitam o recorte temporal
 * {@link PeriodoDashboard}; as de estado corrente (fiéis ativos, projetos por status, novos fiéis
 * no mês) o ignoram. O escopo organizacional vem de {@link EscopoDashboardResolver}: a organização
 * pedida no filtro, quando o ator tem acesso a ela, senão a organização do próprio ator.
 */
@Service
public class GetDashboardMetricsUseCase {

    private static final String STATUS_EVENTO_PENDENTE = "RASCUNHO";
    private static final int PERCENTUAL_SCALE = 1;
    private static final Set<ProjetoStatus> STATUS_ATIVOS =
            EnumSet.of(ProjetoStatus.ABERTO, ProjetoStatus.EM_ANDAMENTO);
    private static final Set<TipoGrupo> TIPOS_ENGAJAMENTO = EnumSet.of(TipoGrupo.PASTORAL, TipoGrupo.MOVIMENTO);

    private final EventoJpaRepository eventoRepository;
    private final ProjetoEventoJpaRepository projetoRepository;
    private final AprovacaoJpaRepository aprovacaoRepository;
    private final ProjetoSolicitacaoAprovacaoJpaRepository projetoAprovacaoRepository;
    private final GrupoParoquialJpaRepository grupoParoquialRepository;
    private final LocalJpaRepository localRepository;
    private final FielJpaRepository fielRepository;
    private final MembroGrupoJpaRepository membroRepository;
    private final ProjetoAgregacaoDomainService projetoAgregacaoDomainService;
    private final JanelaTemporalResolver janelaResolver;
    private final EscopoDashboardResolver escopoResolver;

    public GetDashboardMetricsUseCase(
            EventoJpaRepository eventoRepository,
            ProjetoEventoJpaRepository projetoRepository,
            AprovacaoJpaRepository aprovacaoRepository,
            ProjetoSolicitacaoAprovacaoJpaRepository projetoAprovacaoRepository,
            GrupoParoquialJpaRepository grupoParoquialRepository,
            LocalJpaRepository localRepository,
            FielJpaRepository fielRepository,
            MembroGrupoJpaRepository membroRepository,
            ProjetoAgregacaoDomainService projetoAgregacaoDomainService,
            JanelaTemporalResolver janelaResolver,
            EscopoDashboardResolver escopoResolver) {
        this.eventoRepository = eventoRepository;
        this.projetoRepository = projetoRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.projetoAprovacaoRepository = projetoAprovacaoRepository;
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.localRepository = localRepository;
        this.fielRepository = fielRepository;
        this.membroRepository = membroRepository;
        this.projetoAgregacaoDomainService = projetoAgregacaoDomainService;
        this.janelaResolver = janelaResolver;
        this.escopoResolver = escopoResolver;
    }

    /**
     * Calcula o snapshot de métricas do dashboard para o ator, o recorte temporal e a organização
     * solicitados.
     *
     * @param actorContext contexto do ator autenticado (define o escopo padrão e o que ele pode ver)
     * @param periodo recorte temporal do filtro global; {@code null} equivale a
     *     {@link PeriodoDashboard#SEMANA}
     * @param grupoParoquialIdSolicitado organização escolhida no filtro, ou {@code null} para usar o
     *     escopo do próprio ator
     * @return snapshot consolidado das métricas
     * @throws org.springframework.security.access.AccessDeniedException se o ator não tiver acesso à
     *     organização solicitada
     */
    @Transactional(readOnly = true)
    public DashboardMetricsDTO execute(
            EventoActorContext actorContext, PeriodoDashboard periodo, UUID grupoParoquialIdSolicitado) {
        Instant agora = Instant.now();
        JanelaTemporal janela = janelaResolver.resolver(periodo, agora);
        UUID escopo = escopoResolver.resolver(actorContext, grupoParoquialIdSolicitado).orElse(null);

        List<EventoEntity> eventosPeriodo = comEscopo(eventoRepository.findAllInPeriod(janela.inicio(), janela.fim()),
                escopo);
        List<ProjetoEventoEntity> projetos = projetoRepository.findAll();
        List<ProjetoEventoEntity> projetosAtivos = filtrarProjetosAtivos(projetos, escopo);
        Map<UUID, String> nomesOrganizacoes = carregarNomesOrganizacoes();

        return new DashboardMetricsDTO(
                resumoGeral(eventosPeriodo, projetosAtivos, agora, escopo),
                distribuicao(eventosPeriodo, nomesOrganizacoes),
                projetosPorStatus(projetos, escopo, agora),
                projetosAtivos.stream().map(p -> detalheProjeto(p, agora)).toList(),
                eventosOrfaos(janela, escopo, projetosAtivos),
                eventosSemana(agora, escopo, nomesOrganizacoes));
    }

    private ResumoGeralDTO resumoGeral(List<EventoEntity> eventosPeriodo, List<ProjetoEventoEntity> projetosAtivos,
                                       Instant agora, UUID escopo) {
        long engajados = membroRepository.contarFieisDistintosEngajados(TIPOS_ENGAJAMENTO);
        long ativos = fielRepository.countByAtivoTrue();
        return new ResumoGeralDTO(
                eventosPeriodo.size(),
                projetosAtivos.size(),
                contarAprovacoesPendentes(escopo),
                ativos,
                fielRepository.countByAtivoTrueAndCriadoEmUtcGreaterThanEqual(inicioDoMes(agora)),
                engajados,
                Math.max(0, ativos - engajados));
    }

    private int contarAprovacoesPendentes(UUID escopo) {
        List<AprovacaoEntity> aprovacoesEvento =
                aprovacaoRepository.findByStatusOrderByCriadoEmUtcAsc(AprovacaoStatus.PENDENTE);
        List<ProjetoSolicitacaoAprovacaoEntity> aprovacoesProjeto =
                projetoAprovacaoRepository.findByStatus(AprovacaoStatus.PENDENTE);
        if (escopo == null) {
            return aprovacoesEvento.size() + aprovacoesProjeto.size();
        }
        return contarNoEscopoEvento(aprovacoesEvento, escopo) + contarNoEscopoProjeto(aprovacoesProjeto, escopo);
    }

    private int contarNoEscopoEvento(List<AprovacaoEntity> aprovacoes, UUID orgId) {
        Set<UUID> ids = aprovacoes.stream()
                .map(AprovacaoEntity::getEventoId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> noEscopo = eventoRepository.findAllById(ids).stream()
                .filter(e -> orgId.equals(e.getGrupoParoquialResponsavelId()))
                .map(EventoEntity::getId).collect(Collectors.toSet());
        return (int) aprovacoes.stream().filter(a -> noEscopo.contains(a.getEventoId())).count();
    }

    private int contarNoEscopoProjeto(List<ProjetoSolicitacaoAprovacaoEntity> aprovacoes, UUID orgId) {
        Set<UUID> ids = aprovacoes.stream()
                .map(ProjetoSolicitacaoAprovacaoEntity::getProjetoId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> noEscopo = projetoRepository.findAllById(ids).stream()
                .filter(p -> orgId.equals(p.getGrupoParoquialResponsavelId()))
                .map(ProjetoEventoEntity::getId).collect(Collectors.toSet());
        return (int) aprovacoes.stream().filter(a -> noEscopo.contains(a.getProjetoId())).count();
    }

    private ProjetosPorStatusDTO projetosPorStatus(List<ProjetoEventoEntity> projetos, UUID escopo,
                                                   Instant agora) {
        List<ProjetoEventoEntity> visiveis = projetos.stream()
                .filter(p -> escopo == null || escopo.equals(p.getGrupoParoquialResponsavelId()))
                .toList();
        int atrasados = (int) visiveis.stream().filter(p -> estaAtrasado(p, agora)).count();
        return new ProjetosPorStatusDTO(
                contarStatus(visiveis, ProjetoStatus.ABERTO),
                contarStatus(visiveis, ProjetoStatus.EM_ANDAMENTO),
                contarStatus(visiveis, ProjetoStatus.CONCLUIDO),
                atrasados);
    }

    private int contarStatus(List<ProjetoEventoEntity> projetos, ProjetoStatus status) {
        return (int) projetos.stream().filter(p -> p.getStatus() == status).count();
    }

    private boolean estaAtrasado(ProjetoEventoEntity p, Instant agora) {
        if (!STATUS_ATIVOS.contains(p.getStatus())) {
            return false;
        }
        if (p.getFimUtc() != null && p.getFimUtc().isBefore(agora)) {
            return true;
        }
        int pendentes = (int) eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(p.getId(), agora);
        return projetoAgregacaoDomainService.verificarSeEstaEmRisco(p.getInicioUtc(), p.getFimUtc(), pendentes);
    }

    private DetalheProjetoDTO detalheProjeto(ProjetoEventoEntity p, Instant agora) {
        int totalEventos = (int) eventoRepository.countByProjetoId(p.getId());
        int realizados = (int) eventoRepository.countByProjetoIdAndFimUtcLessThan(p.getId(), agora);
        int pendentes = (int) eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(p.getId(), agora);
        int pendentesAprovacao = (int) eventoRepository.countByProjetoIdAndStatus(p.getId(), STATUS_EVENTO_PENDENTE);
        return new DetalheProjetoDTO(
                p.getNome(),
                p.getStatus().name(),
                projetoAgregacaoDomainService.calcularPercentualTempoDecorrido(p.getInicioUtc(), p.getFimUtc()),
                totalEventos,
                realizados,
                calcularPercentualExecucao(realizados, totalEventos),
                pendentesAprovacao,
                projetoAgregacaoDomainService.verificarSeEstaEmRisco(p.getInicioUtc(), p.getFimUtc(), pendentes),
                proximoMarco(p.getId(), agora),
                eventoRepository.findInvolvedOrganizationNames(p.getId()));
    }

    /** Percentual de eventos realizados sobre o total; {@code ZERO} se não houver eventos. */
    private BigDecimal calcularPercentualExecucao(int realizados, int totalEventos) {
        return totalEventos == 0
                ? BigDecimal.ZERO.setScale(PERCENTUAL_SCALE, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(realizados)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalEventos), PERCENTUAL_SCALE, RoundingMode.HALF_UP);
    }

    private ProximoMarcoDTO proximoMarco(UUID projetoId, Instant agora) {
        return eventoRepository.findProximoMarco(projetoId, agora)
                .map(e -> new ProximoMarcoDTO(
                        e.getTitulo(),
                        LocalDateTime.ofInstant(e.getInicioUtc(), ZoneId.systemDefault()),
                        e.getNivelPrioridade() == null ? 4 : e.getNivelPrioridade()))
                .orElse(null);
    }

    private List<EventoOrfaoDTO> eventosOrfaos(JanelaTemporal janela, UUID escopo,
                                               List<ProjetoEventoEntity> projetosAtivos) {
        return comEscopo(eventoRepository.findOrfaosNoPeriodo(janela.inicio(), janela.fim()), escopo).stream()
                .map(e -> {
                    Optional<ProjetoEventoEntity> sugestao = sugerirProjeto(e, projetosAtivos);
                    return new EventoOrfaoDTO(
                            e.getId(),
                            e.getTitulo(),
                            LocalDateTime.ofInstant(e.getInicioUtc(), ZoneId.systemDefault()),
                            sugestao.map(ProjetoEventoEntity::getId).orElse(null),
                            sugestao.map(ProjetoEventoEntity::getNome).orElse(null));
                })
                .toList();
    }

    private Optional<ProjetoEventoEntity> sugerirProjeto(
            EventoEntity evento, List<ProjetoEventoEntity> projetosAtivos) {
        return projetosAtivos.stream()
                .filter(p -> p.getInicioUtc() != null && p.getFimUtc() != null)
                .filter(p -> evento.getGrupoParoquialResponsavelId().equals(p.getGrupoParoquialResponsavelId()))
                .filter(p -> !evento.getInicioUtc().isBefore(p.getInicioUtc())
                        && evento.getInicioUtc().isBefore(p.getFimUtc()))
                .findFirst();
    }

    private List<DistribuicaoEventosDTO> distribuicao(List<EventoEntity> eventos, Map<UUID, String> nomes) {
        return eventos.stream()
                .collect(Collectors.groupingBy(EventoEntity::getGrupoParoquialResponsavelId, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new DistribuicaoEventosDTO(nomes.getOrDefault(e.getKey(), "Organização Desconhecida"),
                        e.getValue()))
                .sorted(Comparator.comparingLong(DistribuicaoEventosDTO::quantidadeEventos).reversed())
                .toList();
    }

    private List<EventoSemanalDTO> eventosSemana(Instant agora, UUID escopo, Map<UUID, String> nomesOrg) {
        ZonedDateTime nowZoned = agora.atZone(ZoneOffset.UTC);
        Instant inicio = nowZoned.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS).toInstant();
        Instant fim = nowZoned.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant();
        Map<UUID, String> nomesLocais = carregarNomesLocais();
        return comEscopo(eventoRepository.findAllInPeriod(inicio, fim), escopo).stream()
                .map(e -> new EventoSemanalDTO(
                        e.getTitulo(),
                        LocalDateTime.ofInstant(e.getInicioUtc(), ZoneId.systemDefault()),
                        nomesLocais.getOrDefault(e.getLocalId(), "Sem Local"),
                        nomesOrg.getOrDefault(e.getGrupoParoquialResponsavelId(), "Organização Desconhecida")))
                .toList();
    }

    private List<ProjetoEventoEntity> filtrarProjetosAtivos(List<ProjetoEventoEntity> projetos, UUID escopo) {
        return projetos.stream()
                .filter(p -> STATUS_ATIVOS.contains(p.getStatus()))
                .filter(p -> escopo == null || escopo.equals(p.getGrupoParoquialResponsavelId()))
                .toList();
    }

    private List<EventoEntity> comEscopo(List<EventoEntity> eventos, UUID escopo) {
        if (escopo == null) {
            return eventos;
        }
        return eventos.stream().filter(e -> escopo.equals(e.getGrupoParoquialResponsavelId())).toList();
    }

    private Map<UUID, String> carregarNomesOrganizacoes() {
        Map<UUID, String> nomes = new HashMap<>();
        grupoParoquialRepository.findAll().forEach(o -> nomes.put(o.getId(), o.getNome()));
        return nomes;
    }

    private Map<UUID, String> carregarNomesLocais() {
        Map<UUID, String> nomes = new HashMap<>();
        localRepository.findAll().forEach(l -> nomes.put(l.getId(), l.getNome()));
        return nomes;
    }

    private Instant inicioDoMes(Instant agora) {
        return agora.atZone(ZoneOffset.UTC)
                .with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS).toInstant();
    }
}
