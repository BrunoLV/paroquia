package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
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
import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Calcula a seção de logística do dashboard: choque de horário por local, taxa de ocupação dos
 * ambientes (estimada), eventos por responsável e pendências de homologação por tipo. Isolada do
 * {@code GetDashboardMetricsUseCase} porque a detecção de choque é uma auto-junção O(n²) sobre
 * eventos e merece ser paga só quando esta seção é aberta.
 */
@Service
public class GetDashboardLogisticaUseCase {

    // ponytail: janela operacional fixa de 14h (06h-20h); extrair para configuração por paróquia
    // se as casas passarem a operar horários muito distintos.
    private static final int HORAS_OPERACIONAIS_DIA = 14;

    private final EventoJpaRepository eventoRepository;
    private final LocalJpaRepository localRepository;
    private final GrupoParoquialJpaRepository grupoParoquialRepository;
    private final AprovacaoJpaRepository aprovacaoRepository;
    private final ProjetoSolicitacaoAprovacaoJpaRepository projetoAprovacaoRepository;
    private final JanelaTemporalResolver janelaResolver;
    private final EscopoDashboardResolver escopoResolver;

    public GetDashboardLogisticaUseCase(
            EventoJpaRepository eventoRepository,
            LocalJpaRepository localRepository,
            GrupoParoquialJpaRepository grupoParoquialRepository,
            AprovacaoJpaRepository aprovacaoRepository,
            ProjetoSolicitacaoAprovacaoJpaRepository projetoAprovacaoRepository,
            JanelaTemporalResolver janelaResolver,
            EscopoDashboardResolver escopoResolver) {
        this.eventoRepository = eventoRepository;
        this.localRepository = localRepository;
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.projetoAprovacaoRepository = projetoAprovacaoRepository;
        this.janelaResolver = janelaResolver;
        this.escopoResolver = escopoResolver;
    }

    /**
     * Monta o snapshot de logística para o ator, o recorte temporal e a organização informados.
     *
     * @param actorContext contexto do ator autenticado
     * @param periodo recorte temporal do filtro global
     * @param grupoParoquialIdSolicitado organização escolhida no filtro, ou {@code null}
     * @return snapshot de logística
     * @throws org.springframework.security.access.AccessDeniedException se o ator não puder ver a
     *     organização solicitada
     */
    @Transactional(readOnly = true)
    public DashboardLogisticaDTO execute(
            EventoActorContext actorContext, PeriodoDashboard periodo, UUID grupoParoquialIdSolicitado) {
        Instant agora = Instant.now();
        JanelaTemporal janela = janelaResolver.resolver(periodo, agora);
        Optional<UUID> escopo = escopoResolver.resolver(actorContext, grupoParoquialIdSolicitado);

        List<LocalEntity> locais = locaisNoEscopo(escopo);
        Map<UUID, String> nomesLocais = locais.stream()
                .collect(Collectors.toMap(LocalEntity::getId, LocalEntity::getNome));

        return new DashboardLogisticaDTO(
                choquesHorario(janela, nomesLocais),
                ocupacaoAmbientes(agora, locais, nomesLocais),
                eventosPorResponsavel(janela, escopo),
                pendenciasHomologacao());
    }

    private List<ChoqueHorarioDTO> choquesHorario(JanelaTemporal janela, Map<UUID, String> nomesLocais) {
        return eventoRepository.findChoquesHorarioPorLocal(janela.inicio(), janela.fim()).stream()
                .filter(linha -> nomesLocais.containsKey((UUID) linha[0]))
                .map(linha -> new ChoqueHorarioDTO(
                        nomesLocais.get((UUID) linha[0]),
                        (String) linha[1],
                        (String) linha[2],
                        LocalDateTime.ofInstant((Instant) linha[3], ZoneId.systemDefault())))
                .toList();
    }

    private OcupacaoAmbientesDTO ocupacaoAmbientes(
            Instant agora, List<LocalEntity> locais, Map<UUID, String> nomesLocais) {
        JanelaTemporal dia = janelaResolver.resolver(PeriodoDashboard.HOJE, agora);
        JanelaTemporal semana = janelaResolver.resolver(PeriodoDashboard.SEMANA, agora);
        Set<UUID> ids = nomesLocais.keySet();
        Map<UUID, Long> minutosDia = minutosPorLocal(dia, ids);
        Map<UUID, Long> minutosSemana = minutosPorLocal(semana, ids);
        return new OcupacaoAmbientesDTO(
                taxa(soma(minutosDia), locais.size(), 1),
                taxa(soma(minutosSemana), locais.size(), 7),
                porLocal(locais, minutosSemana));
    }

    private Map<UUID, Long> minutosPorLocal(JanelaTemporal janela, Set<UUID> idsPermitidos) {
        return eventoRepository.findComLocalNoPeriodo(janela.inicio(), janela.fim()).stream()
                .filter(e -> idsPermitidos.contains(e.getLocalId()))
                .collect(Collectors.groupingBy(EventoEntity::getLocalId,
                        Collectors.summingLong(e -> Duration.between(e.getInicioUtc(), e.getFimUtc()).toMinutes())));
    }

    private List<OcupacaoLocalDTO> porLocal(List<LocalEntity> locais, Map<UUID, Long> minutosSemana) {
        double capacidadeMinutos = HORAS_OPERACIONAIS_DIA * 60.0 * 7;
        return locais.stream()
                .map(l -> {
                    long minutos = minutosSemana.getOrDefault(l.getId(), 0L);
                    return new OcupacaoLocalDTO(l.getNome(),
                            clamp(minutos * 100.0 / capacidadeMinutos), minutos / 60);
                })
                .sorted(Comparator.comparingDouble(OcupacaoLocalDTO::taxaSemanaPercentual).reversed())
                .toList();
    }

    private double taxa(long minutosOcupados, int quantidadeLocais, int dias) {
        if (quantidadeLocais == 0) {
            return 0.0;
        }
        double capacidade = quantidadeLocais * HORAS_OPERACIONAIS_DIA * 60.0 * dias;
        return clamp(minutosOcupados * 100.0 / capacidade);
    }

    private List<EventosPorResponsavelDTO> eventosPorResponsavel(JanelaTemporal janela, Optional<UUID> escopo) {
        Map<UUID, String> nomes = new HashMap<>();
        grupoParoquialRepository.findAll().forEach(o -> nomes.put(o.getId(), o.getNome()));
        return eventoRepository.contarPorResponsavelNoPeriodo(janela.inicio(), janela.fim()).stream()
                .filter(linha -> escopo.isEmpty() || escopo.get().equals(linha[0]))
                .map(linha -> new EventosPorResponsavelDTO(
                        nomes.getOrDefault((UUID) linha[0], "Organização Desconhecida"),
                        ((Number) linha[1]).longValue()))
                .sorted(Comparator.comparingLong(EventosPorResponsavelDTO::quantidade).reversed())
                .toList();
    }

    private List<PendenciaHomologacaoDTO> pendenciasHomologacao() {
        Map<String, Long> porTipo = new HashMap<>();
        Stream.concat(
                        aprovacaoRepository.contarPorTipoComStatus(AprovacaoStatus.PENDENTE).stream(),
                        projetoAprovacaoRepository.contarPorTipoComStatus(AprovacaoStatus.PENDENTE).stream())
                .forEach(l -> porTipo.merge((String) l[0], ((Number) l[1]).longValue(), Long::sum));
        return porTipo.entrySet().stream()
                .map(e -> new PendenciaHomologacaoDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(PendenciaHomologacaoDTO::quantidade).reversed())
                .toList();
    }

    private List<LocalEntity> locaisNoEscopo(Optional<UUID> escopo) {
        return localRepository.findByAtivoTrue().stream()
                .filter(l -> escopo.isEmpty() || escopo.get().equals(l.getGrupoParoquialId()))
                .toList();
    }

    private long soma(Map<UUID, Long> minutos) {
        return minutos.values().stream().mapToLong(Long::longValue).sum();
    }

    private double clamp(double percentual) {
        return Math.max(0.0, Math.min(100.0, percentual));
    }
}
