package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.ChoqueHorarioDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardLogisticaDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.EventosPorResponsavelDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.OcupacaoAmbientesDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.OcupacaoLocalDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.PendenciaHomologacaoDTO;
import br.com.nsfatima.gestao.dashboard.application.service.EscopoDashboardResolver;
import br.com.nsfatima.gestao.dashboard.domain.model.JanelaTemporal;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.dashboard.domain.service.CalculadoraOcupacaoAmbiente;
import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ReservaLocalIntervalo;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.repository.PainelConsultaJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Calcula a seção de logística do dashboard: choque de horário por local, taxa de ocupação dos
 * ambientes, eventos por responsável e pendências de homologação por tipo. Isolada do
 * {@code ObterDashboardMetricasUseCase} porque a detecção de choque é uma auto-junção O(n²) sobre
 * eventos e merece ser paga só quando esta seção é aberta.
 *
 * <p>A ocupação dos ambientes é derivada das <strong>reservas de local</strong> (a fonte de verdade
 * de quem ocupa o quê), recortadas na janela e com as sobreposições unidas; com escopo, só contam as
 * reservas solicitadas pela organização do escopo (a organização responsável pelo evento), sobre o
 * conjunto de todos os ambientes ativos. As pendências de homologação respeitam o mesmo escopo.
 */
@Service
public class ObterDashboardLogisticaUseCase {

    // ponytail: janela operacional fixa de 14h (06h-20h); extrair para configuração por paróquia
    // se as casas passarem a operar horários muito distintos.
    private static final int HORAS_OPERACIONAIS_DIA = 14;
    private static final int PERCENTUAL_SCALE = 1;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal ZERO_SCALED = BigDecimal.ZERO.setScale(PERCENTUAL_SCALE, RoundingMode.HALF_UP);
    private static final BigDecimal CEM_SCALED = CEM.setScale(PERCENTUAL_SCALE, RoundingMode.HALF_UP);

    private final CalculadoraOcupacaoAmbiente calculadoraOcupacao = new CalculadoraOcupacaoAmbiente();
    private final EventoJpaRepository eventoRepository;
    private final PainelConsultaJpaRepository painelRepository;
    private final LocalJpaRepository localRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final JanelaTemporalResolver janelaResolver;
    private final EscopoDashboardResolver escopoResolver;
    private final RelogioNegocio relogio;

    public ObterDashboardLogisticaUseCase(
            EventoJpaRepository eventoRepository,
            PainelConsultaJpaRepository painelRepository,
            LocalJpaRepository localRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            JanelaTemporalResolver janelaResolver,
            EscopoDashboardResolver escopoResolver,
            RelogioNegocio relogio) {
        this.eventoRepository = eventoRepository;
        this.painelRepository = painelRepository;
        this.localRepository = localRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.janelaResolver = janelaResolver;
        this.escopoResolver = escopoResolver;
        this.relogio = relogio;
    }

    /**
     * Monta o snapshot de logística para o ator, o recorte temporal e a organização informados.
     *
     * @param actorContext contexto do ator autenticado
     * @param periodo recorte temporal do filtro global
     * @param organizacaoIdSolicitado organização escolhida no filtro, ou {@code null}
     * @return snapshot de logística
     * @throws org.springframework.security.access.AccessDeniedException se o ator não puder ver a
     *     organização solicitada
     */
    @Transactional(readOnly = true)
    public DashboardLogisticaDTO execute(
            EventoActorContext actorContext, PeriodoDashboard periodo, UUID organizacaoIdSolicitado) {
        Instant agora = relogio.agora();
        JanelaTemporal janela = janelaResolver.resolver(periodo, agora);
        UUID escopo = escopoResolver.resolver(actorContext, organizacaoIdSolicitado).orElse(null);

        List<LocalEntity> ambientes = localRepository.findByAtivoTrue();
        Map<UUID, String> nomesLocaisDoEscopo = locaisNoEscopo(ambientes, escopo).stream()
                .collect(Collectors.toMap(LocalEntity::getId, LocalEntity::getNome));

        return new DashboardLogisticaDTO(
                choquesHorario(janela, nomesLocaisDoEscopo),
                ocupacaoAmbientes(agora, ambientes, escopo),
                eventosPorResponsavel(janela, escopo),
                pendenciasHomologacao(escopo));
    }

    private List<ChoqueHorarioDTO> choquesHorario(JanelaTemporal janela, Map<UUID, String> nomesLocais) {
        return eventoRepository.findChoquesHorarioPorLocal(janela.inicio(), janela.fim()).stream()
                .filter(linha -> nomesLocais.containsKey((UUID) linha[0]))
                .map(linha -> new ChoqueHorarioDTO(
                        nomesLocais.get((UUID) linha[0]),
                        (String) linha[1],
                        (String) linha[2],
                        LocalDateTime.ofInstant((Instant) linha[3], relogio.fuso())))
                .toList();
    }

    private OcupacaoAmbientesDTO ocupacaoAmbientes(Instant agora, List<LocalEntity> ambientes, UUID escopo) {
        JanelaTemporal dia = janelaResolver.resolver(PeriodoDashboard.HOJE, agora);
        JanelaTemporal semana = janelaResolver.resolver(PeriodoDashboard.SEMANA, agora);
        Set<UUID> ids = ambientes.stream().map(LocalEntity::getId).collect(Collectors.toSet());
        Map<UUID, Long> minutosDia = minutosPorLocal(dia, ids, escopo);
        Map<UUID, Long> minutosSemana = minutosPorLocal(semana, ids, escopo);
        return new OcupacaoAmbientesDTO(
                taxa(soma(minutosDia), ambientes.size(), 1),
                taxa(soma(minutosSemana), ambientes.size(), 7),
                porLocal(ambientes, minutosSemana));
    }

    /** Minutos ocupados por local na janela, a partir das reservas (sobreposições unidas). */
    private Map<UUID, Long> minutosPorLocal(JanelaTemporal janela, Set<UUID> idsPermitidos, UUID escopo) {
        Map<UUID, List<JanelaTemporal>> reservasPorLocal = painelRepository
                .reservasDeLocalNaJanela(janela.inicio(), janela.fim(), escopo).stream()
                .filter(r -> idsPermitidos.contains(r.localId()))
                .collect(Collectors.groupingBy(ReservaLocalIntervalo::localId, Collectors.mapping(
                        r -> new JanelaTemporal(r.inicio(), r.fim()), Collectors.toList())));
        Map<UUID, Long> minutos = new HashMap<>();
        reservasPorLocal.forEach((localId, reservas) ->
                minutos.put(localId, calculadoraOcupacao.minutosOcupados(reservas, janela)));
        return minutos;
    }

    private List<OcupacaoLocalDTO> porLocal(List<LocalEntity> locais, Map<UUID, Long> minutosSemana) {
        BigDecimal capacidadeMinutos = BigDecimal.valueOf(HORAS_OPERACIONAIS_DIA * 60L * 7);
        return locais.stream()
                .map(l -> {
                    long minutos = minutosSemana.getOrDefault(l.getId(), 0L);
                    BigDecimal taxaLocal = clamp(BigDecimal.valueOf(minutos).multiply(CEM)
                            .divide(capacidadeMinutos, PERCENTUAL_SCALE, RoundingMode.HALF_UP));
                    return new OcupacaoLocalDTO(l.getNome(), taxaLocal, minutos / 60);
                })
                .sorted(Comparator.comparing(OcupacaoLocalDTO::taxaSemanaPercentual).reversed())
                .toList();
    }

    private BigDecimal taxa(long minutosOcupados, int quantidadeLocais, int dias) {
        if (quantidadeLocais == 0) {
            return ZERO_SCALED;
        }
        BigDecimal capacidade = BigDecimal.valueOf((long) quantidadeLocais * HORAS_OPERACIONAIS_DIA * 60L * dias);
        return clamp(BigDecimal.valueOf(minutosOcupados).multiply(CEM)
                .divide(capacidade, PERCENTUAL_SCALE, RoundingMode.HALF_UP));
    }

    private List<EventosPorResponsavelDTO> eventosPorResponsavel(JanelaTemporal janela, UUID escopo) {
        List<Object[]> linhas = eventoRepository.contarPorResponsavelNoPeriodo(janela.inicio(), janela.fim())
                .stream()
                .filter(linha -> escopo == null || escopo.equals(linha[0]))
                .toList();
        Set<UUID> ids = linhas.stream().map(linha -> (UUID) linha[0]).collect(Collectors.toSet());
        Map<UUID, String> nomes = ids.isEmpty() ? Map.of() : organizacaoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(OrganizacaoEntity::getId, OrganizacaoEntity::getNome));
        return linhas.stream()
                .map(linha -> new EventosPorResponsavelDTO(
                        nomes.getOrDefault((UUID) linha[0], "Organização Desconhecida"),
                        ((Number) linha[1]).longValue()))
                .sorted(Comparator.comparingLong(EventosPorResponsavelDTO::quantidade).reversed())
                .toList();
    }

    private List<PendenciaHomologacaoDTO> pendenciasHomologacao(UUID escopo) {
        Map<String, Long> porTipo = new HashMap<>();
        Stream.concat(
                        painelRepository.contarAprovacoesDeEventoPorTipo(AprovacaoStatus.PENDENTE, escopo).stream(),
                        painelRepository.contarSolicitacoesDeProjetoPorTipo(AprovacaoStatus.PENDENTE, escopo)
                                .stream())
                .forEach(l -> porTipo.merge((String) l[0], ((Number) l[1]).longValue(), Long::sum));
        return porTipo.entrySet().stream()
                .map(e -> new PendenciaHomologacaoDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(PendenciaHomologacaoDTO::quantidade).reversed())
                .toList();
    }

    private List<LocalEntity> locaisNoEscopo(List<LocalEntity> ambientes, UUID escopo) {
        return ambientes.stream()
                .filter(l -> escopo == null || escopo.equals(l.getOrganizacaoId()))
                .toList();
    }

    private long soma(Map<UUID, Long> minutos) {
        return minutos.values().stream().mapToLong(Long::longValue).sum();
    }

    private BigDecimal clamp(BigDecimal percentual) {
        return percentual.max(ZERO_SCALED).min(CEM_SCALED);
    }
}
