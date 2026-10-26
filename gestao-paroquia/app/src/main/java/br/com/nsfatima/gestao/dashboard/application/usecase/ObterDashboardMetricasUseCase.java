package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricasDTO;
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
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ContagemPorProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.EstatisticaEventosProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.repository.PainelConsultaJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agrega, em uma única leitura, as métricas das seções "crítica" e "projetos" do dashboard
 * gerencial. Não existe tabela de métricas persistida: tudo é calculado sob demanda a partir dos
 * contextos de calendário, projeto, aprovação, fiel e membro. As métricas de volume (eventos do
 * período, distribuição por organização, eventos órfãos) respeitam o recorte temporal
 * {@link PeriodoDashboard}; as de estado corrente (fiéis ativos, projetos por status, novos fiéis
 * no mês) o ignoram. O escopo organizacional vem de {@link EscopoDashboardResolver}: a organização
 * pedida no filtro, quando o ator tem acesso a ela, senão a organização do próprio ator.
 *
 * <p>O escopo vale para <strong>todas</strong> as métricas: eventos, projetos, aprovações e também
 * fiéis (com escopo, "fiéis" são os fiéis ativos com vínculo ativo na organização). Eventos
 * cancelados não entram. A consulta de projetos é agregada (uma consulta por tipo de indicador para
 * todos os projetos), nunca uma por projeto, e dia/mês/semana são os do fuso da paróquia.
 */
@Service
public class ObterDashboardMetricasUseCase {

    private static final int PERCENTUAL_SCALE = 1;
    private static final Set<ProjetoStatus> STATUS_ATIVOS =
            EnumSet.of(ProjetoStatus.ABERTO, ProjetoStatus.EM_ANDAMENTO);
    private static final Set<TipoOrganizacao> TIPOS_ENGAJAMENTO =
            EnumSet.of(TipoOrganizacao.PASTORAL, TipoOrganizacao.MOVIMENTO);
    private static final String ORGANIZACAO_DESCONHECIDA = "Organização Desconhecida";

    private final EventoJpaRepository eventoRepository;
    private final PainelConsultaJpaRepository painelRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final LocalJpaRepository localRepository;
    private final FielJpaRepository fielRepository;
    private final MembroGrupoJpaRepository membroRepository;
    private final ProjetoAgregacaoDomainService projetoAgregacaoDomainService;
    private final JanelaTemporalResolver janelaResolver;
    private final EscopoDashboardResolver escopoResolver;
    private final RelogioNegocio relogio;

    public ObterDashboardMetricasUseCase(
            EventoJpaRepository eventoRepository,
            PainelConsultaJpaRepository painelRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            LocalJpaRepository localRepository,
            FielJpaRepository fielRepository,
            MembroGrupoJpaRepository membroRepository,
            ProjetoAgregacaoDomainService projetoAgregacaoDomainService,
            JanelaTemporalResolver janelaResolver,
            EscopoDashboardResolver escopoResolver,
            RelogioNegocio relogio) {
        this.eventoRepository = eventoRepository;
        this.painelRepository = painelRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.localRepository = localRepository;
        this.fielRepository = fielRepository;
        this.membroRepository = membroRepository;
        this.projetoAgregacaoDomainService = projetoAgregacaoDomainService;
        this.janelaResolver = janelaResolver;
        this.escopoResolver = escopoResolver;
        this.relogio = relogio;
    }

    /**
     * Calcula o snapshot de métricas do dashboard para o ator, o recorte temporal e a organização
     * solicitados.
     *
     * @param actorContext contexto do ator autenticado (define o escopo padrão e o que ele pode ver)
     * @param periodo recorte temporal do filtro global; {@code null} equivale a
     *     {@link PeriodoDashboard#SEMANA}
     * @param organizacaoIdSolicitado organização escolhida no filtro, ou {@code null} para usar o
     *     escopo do próprio ator
     * @return snapshot consolidado das métricas
     * @throws org.springframework.security.access.AccessDeniedException se o ator não tiver acesso à
     *     organização solicitada
     */
    @Transactional(readOnly = true)
    public DashboardMetricasDTO execute(
            EventoActorContext actorContext, PeriodoDashboard periodo, UUID organizacaoIdSolicitado) {
        Instant agora = relogio.agora();
        JanelaTemporal janela = janelaResolver.resolver(periodo, agora);
        UUID escopo = escopoResolver.resolver(actorContext, organizacaoIdSolicitado).orElse(null);

        List<EventoEntity> eventosPeriodo = comEscopo(
                eventoRepository.findAllInPeriod(janela.inicio(), janela.fim()), escopo);
        List<EventoEntity> eventosSemana = comEscopo(eventosDaSemana(), escopo);
        List<EventoEntity> orfaos = comEscopo(eventoRepository.findOrfaosNoPeriodo(janela.inicio(), janela.fim()),
                escopo);
        List<ProjetoEventoEntity> projetosAtivos = painelRepository.findProjetos(STATUS_ATIVOS, escopo);
        IndicadoresProjetos indicadores = carregarIndicadores(projetosAtivos, agora);

        Map<UUID, String> nomesOrganizacoes = nomesOrganizacoes(eventosPeriodo, eventosSemana);

        return new DashboardMetricasDTO(
                resumoGeral(eventosPeriodo.size(), projetosAtivos.size(), escopo),
                distribuicao(eventosPeriodo, nomesOrganizacoes),
                projetosPorStatus(projetosAtivos, indicadores, escopo, agora),
                projetosAtivos.stream().map(p -> detalheProjeto(p, indicadores, agora)).toList(),
                eventosOrfaos(orfaos, projetosAtivos),
                eventosSemana(eventosSemana, nomesOrganizacoes));
    }

    private ResumoGeralDTO resumoGeral(int eventosNoPeriodo, int projetosAtivos, UUID escopo) {
        Instant inicioDoMes = relogio.inicioDoMes();
        long ativos;
        long novos;
        long engajados;
        if (escopo == null) {
            ativos = fielRepository.countByAtivoTrue();
            novos = fielRepository.countByAtivoTrueAndCriadoEmUtcGreaterThanEqual(inicioDoMes);
            engajados = membroRepository.contarFieisDistintosEngajados(TIPOS_ENGAJAMENTO);
        } else {
            ativos = painelRepository.contarFieisAtivosDaOrganizacao(escopo);
            novos = painelRepository.contarFieisNovosDaOrganizacao(escopo, inicioDoMes);
            engajados = painelRepository.contarFieisEngajadosDaOrganizacao(escopo, TIPOS_ENGAJAMENTO);
        }
        return new ResumoGeralDTO(
                eventosNoPeriodo,
                projetosAtivos,
                contarAprovacoesPendentes(escopo),
                ativos,
                novos,
                engajados,
                Math.max(0, ativos - engajados));
    }

    private int contarAprovacoesPendentes(UUID escopo) {
        return (int) (painelRepository.contarAprovacoesDeEvento(AprovacaoStatus.PENDENTE, escopo)
                + painelRepository.contarSolicitacoesDeProjeto(AprovacaoStatus.PENDENTE, escopo));
    }

    private ProjetosPorStatusDTO projetosPorStatus(List<ProjetoEventoEntity> projetosAtivos,
                                                   IndicadoresProjetos indicadores, UUID escopo, Instant agora) {
        Map<ProjetoStatus, Long> porStatus = new HashMap<>();
        for (Object[] linha : painelRepository.contarProjetosPorStatus(escopo)) {
            porStatus.put((ProjetoStatus) linha[0], ((Number) linha[1]).longValue());
        }
        int atrasados = (int) projetosAtivos.stream().filter(p -> estaAtrasado(p, indicadores, agora)).count();
        return new ProjetosPorStatusDTO(
                porStatus.getOrDefault(ProjetoStatus.ABERTO, 0L).intValue(),
                porStatus.getOrDefault(ProjetoStatus.EM_ANDAMENTO, 0L).intValue(),
                porStatus.getOrDefault(ProjetoStatus.CONCLUIDO, 0L).intValue(),
                atrasados);
    }

    private boolean estaAtrasado(ProjetoEventoEntity p, IndicadoresProjetos indicadores, Instant agora) {
        if (p.getFimUtc() != null && p.getFimUtc().isBefore(agora)) {
            return true;
        }
        return projetoAgregacaoDomainService.verificarSeEstaEmRisco(
                p.getInicioUtc(), p.getFimUtc(), (int) indicadores.estatistica(p.getId()).pendentes());
    }

    private DetalheProjetoDTO detalheProjeto(ProjetoEventoEntity p, IndicadoresProjetos indicadores, Instant agora) {
        Estatistica estatistica = indicadores.estatistica(p.getId());
        int total = (int) estatistica.total();
        int realizados = (int) estatistica.realizados();
        return new DetalheProjetoDTO(
                p.getNome(),
                p.getStatus().name(),
                projetoAgregacaoDomainService.calcularPercentualTempoDecorrido(p.getInicioUtc(), p.getFimUtc()),
                total,
                realizados,
                calcularPercentualExecucao(realizados, total),
                indicadores.aprovacoesPendentes().getOrDefault(p.getId(), 0L).intValue(),
                projetoAgregacaoDomainService.verificarSeEstaEmRisco(
                        p.getInicioUtc(), p.getFimUtc(), (int) estatistica.pendentes()),
                proximoMarco(indicadores.proximosMarcos().get(p.getId())),
                indicadores.envolvidos().getOrDefault(p.getId(), List.of()));
    }

    /** Percentual de eventos realizados sobre o total; {@code ZERO} se não houver eventos. */
    private BigDecimal calcularPercentualExecucao(int realizados, int totalEventos) {
        return totalEventos == 0
                ? BigDecimal.ZERO.setScale(PERCENTUAL_SCALE, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(realizados)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalEventos), PERCENTUAL_SCALE, RoundingMode.HALF_UP);
    }

    private ProximoMarcoDTO proximoMarco(EventoEntity evento) {
        if (evento == null) {
            return null;
        }
        return new ProximoMarcoDTO(
                evento.getTitulo(),
                LocalDateTime.ofInstant(evento.getInicioUtc(), relogio.fuso()),
                evento.getNivelPrioridade() == null ? 4 : evento.getNivelPrioridade());
    }

    private List<EventoOrfaoDTO> eventosOrfaos(List<EventoEntity> orfaos, List<ProjetoEventoEntity> projetosAtivos) {
        return orfaos.stream()
                .map(e -> {
                    Optional<ProjetoEventoEntity> sugestao = sugerirProjeto(e, projetosAtivos);
                    return new EventoOrfaoDTO(
                            e.getId(),
                            e.getTitulo(),
                            LocalDateTime.ofInstant(e.getInicioUtc(), relogio.fuso()),
                            sugestao.map(ProjetoEventoEntity::getId).orElse(null),
                            sugestao.map(ProjetoEventoEntity::getNome).orElse(null));
                })
                .toList();
    }

    private Optional<ProjetoEventoEntity> sugerirProjeto(
            EventoEntity evento, List<ProjetoEventoEntity> projetosAtivos) {
        return projetosAtivos.stream()
                .filter(p -> p.getInicioUtc() != null && p.getFimUtc() != null)
                .filter(p -> evento.getOrganizacaoResponsavelId().equals(p.getOrganizacaoResponsavelId()))
                .filter(p -> !evento.getInicioUtc().isBefore(p.getInicioUtc())
                        && evento.getInicioUtc().isBefore(p.getFimUtc()))
                .findFirst();
    }

    private List<DistribuicaoEventosDTO> distribuicao(List<EventoEntity> eventos, Map<UUID, String> nomes) {
        return eventos.stream()
                .collect(Collectors.groupingBy(EventoEntity::getOrganizacaoResponsavelId, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new DistribuicaoEventosDTO(nomes.getOrDefault(e.getKey(), ORGANIZACAO_DESCONHECIDA),
                        e.getValue()))
                .sorted(Comparator.comparingLong(DistribuicaoEventosDTO::quantidadeEventos).reversed())
                .toList();
    }

    /** Eventos da semana civil corrente (segunda a domingo) no fuso da paróquia. */
    private List<EventoEntity> eventosDaSemana() {
        LocalDate segunda = relogio.hoje().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Instant inicio = relogio.inicioDoDia(segunda);
        Instant fim = relogio.inicioDoDia(segunda.plusDays(7));
        return eventoRepository.findAllInPeriod(inicio, fim.minusNanos(1));
    }

    private List<EventoSemanalDTO> eventosSemana(List<EventoEntity> eventos, Map<UUID, String> nomesOrg) {
        Set<UUID> idsLocais = eventos.stream().map(EventoEntity::getLocalId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> nomesLocais = new HashMap<>();
        if (!idsLocais.isEmpty()) {
            localRepository.findAllById(idsLocais).forEach(l -> nomesLocais.put(l.getId(), l.getNome()));
        }
        return eventos.stream()
                .map(e -> new EventoSemanalDTO(
                        e.getTitulo(),
                        LocalDateTime.ofInstant(e.getInicioUtc(), relogio.fuso()),
                        nomesLocais.getOrDefault(e.getLocalId(), "Sem Local"),
                        nomesOrg.getOrDefault(e.getOrganizacaoResponsavelId(), ORGANIZACAO_DESCONHECIDA)))
                .toList();
    }

    private List<EventoEntity> comEscopo(List<EventoEntity> eventos, UUID escopo) {
        if (escopo == null) {
            return eventos;
        }
        return eventos.stream().filter(e -> escopo.equals(e.getOrganizacaoResponsavelId())).toList();
    }

    /** Resolve apenas os nomes das organizações que aparecem nos eventos exibidos (sem varrer a tabela). */
    private Map<UUID, String> nomesOrganizacoes(List<EventoEntity> periodo, List<EventoEntity> semana) {
        Set<UUID> ids = new HashSet<>();
        periodo.forEach(e -> ids.add(e.getOrganizacaoResponsavelId()));
        semana.forEach(e -> ids.add(e.getOrganizacaoResponsavelId()));
        ids.remove(null);
        Map<UUID, String> nomes = new HashMap<>();
        if (!ids.isEmpty()) {
            organizacaoRepository.findAllById(ids).forEach(o -> nomes.put(o.getId(), o.getNome()));
        }
        return nomes;
    }

    private IndicadoresProjetos carregarIndicadores(List<ProjetoEventoEntity> projetosAtivos, Instant agora) {
        Set<UUID> ids = projetosAtivos.stream().map(ProjetoEventoEntity::getId).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return IndicadoresProjetos.VAZIO;
        }
        Map<UUID, Estatistica> estatisticas = painelRepository.estatisticasEventosPorProjeto(ids, agora).stream()
                .collect(Collectors.toMap(EstatisticaEventosProjeto::projetoId, this::paraEstatistica));
        Map<UUID, Long> aprovacoes = painelRepository.contarAprovacoesPorProjeto(ids, AprovacaoStatus.PENDENTE)
                .stream().collect(Collectors.toMap(ContagemPorProjeto::projetoId, ContagemPorProjeto::quantidade));
        Map<UUID, EventoEntity> marcos = painelRepository.eventosFuturosDosProjetos(ids, agora).stream()
                .collect(Collectors.toMap(
                        EventoEntity::getProjetoId, Function.identity(), (primeiro, outro) -> primeiro));
        Map<UUID, List<String>> envolvidos = new HashMap<>();
        for (Object[] linha : painelRepository.nomesOrganizacoesEnvolvidasPorProjeto(ids)) {
            if (linha[1] != null) {
                envolvidos.computeIfAbsent((UUID) linha[0], k -> new ArrayList<>()).add((String) linha[1]);
            }
        }
        return new IndicadoresProjetos(estatisticas, aprovacoes, marcos, envolvidos);
    }

    private Estatistica paraEstatistica(EstatisticaEventosProjeto linha) {
        return new Estatistica(linha.total(), linha.realizados(), linha.pendentes());
    }

    /** Contagens de eventos de um projeto. */
    private record Estatistica(long total, long realizados, long pendentes) {
        static final Estatistica ZERO = new Estatistica(0, 0, 0);
    }

    /** Indicadores de todos os projetos ativos, carregados em lote. */
    private record IndicadoresProjetos(
            Map<UUID, Estatistica> estatisticas,
            Map<UUID, Long> aprovacoesPendentes,
            Map<UUID, EventoEntity> proximosMarcos,
            Map<UUID, List<String>> envolvidos) {

        static final IndicadoresProjetos VAZIO = new IndicadoresProjetos(Map.of(), Map.of(), Map.of(), Map.of());

        Estatistica estatistica(UUID projetoId) {
            return estatisticas.getOrDefault(projetoId, Estatistica.ZERO);
        }
    }
}
