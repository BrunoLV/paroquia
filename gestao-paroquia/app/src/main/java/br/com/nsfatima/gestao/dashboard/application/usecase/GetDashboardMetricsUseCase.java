package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricsDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.EventDistributionDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.GeneralSummaryDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.ProjectDetailDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.WeeklyEventDTO;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetDashboardMetricsUseCase {

    private final EventoJpaRepository eventoRepository;
    private final ProjetoEventoJpaRepository projetoRepository;
    private final AprovacaoJpaRepository aprovacaoRepository;
    private final ProjetoSolicitacaoAprovacaoJpaRepository projetoAprovacaoRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final LocalJpaRepository localRepository;
    private final ProjetoAgregacaoDomainService projetoAgregacaoDomainService;

    public GetDashboardMetricsUseCase(
            EventoJpaRepository eventoRepository,
            ProjetoEventoJpaRepository projetoRepository,
            AprovacaoJpaRepository aprovacaoRepository,
            ProjetoSolicitacaoAprovacaoJpaRepository projetoAprovacaoRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            LocalJpaRepository localRepository,
            ProjetoAgregacaoDomainService projetoAgregacaoDomainService) {
        this.eventoRepository = eventoRepository;
        this.projetoRepository = projetoRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.projetoAprovacaoRepository = projetoAprovacaoRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.localRepository = localRepository;
        this.projetoAgregacaoDomainService = projetoAgregacaoDomainService;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsDTO execute(EventoActorContext actorContext) {
        UUID orgId = actorContext.organizationId();
        boolean isFilterNeeded = orgId != null;

        Instant agora = Instant.now();
        ZonedDateTime nowZoned = ZonedDateTime.now(ZoneOffset.UTC);

        // 1. Calculate current month boundaries
        ZonedDateTime startOfMonth = nowZoned.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime endOfMonth =
                nowZoned.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).truncatedTo(ChronoUnit.DAYS);
        Instant inicioMes = startOfMonth.toInstant();
        Instant fimMes = endOfMonth.toInstant();

        // 2. Fetch events in current month
        List<EventoEntity> eventosMes = eventoRepository.findAllInPeriod(inicioMes, fimMes);
        if (isFilterNeeded) {
            eventosMes = eventosMes.stream()
                    .filter(e -> orgId.equals(e.getOrganizacaoResponsavelId()))
                    .toList();
        }

        // 3. Fetch all projects
        List<ProjetoEventoEntity> projetos = projetoRepository.findAll();
        List<ProjetoEventoEntity> projetosAtivosEntities = projetos.stream()
                .filter(p -> "ABERTO".equals(p.getStatus()) || "EM_ANDAMENTO".equals(p.getStatus()))
                .filter(p -> !isFilterNeeded || orgId.equals(p.getOrganizacaoResponsavelId()))
                .toList();

        // 4. Count pending approvals
        // Event approvals
        List<AprovacaoEntity> aprovacoesPendentes =
                aprovacaoRepository.findByStatusIgnoreCaseOrderByCriadoEmUtcAsc("PENDENTE");
        long eventApprovalsCount = aprovacoesPendentes.stream()
                .filter(a -> {
                    if (!isFilterNeeded) {
                        return true;
                    }
                    if (a.getEventoId() == null) {
                        return false;
                    }
                    Optional<EventoEntity> ev = eventoRepository.findById(a.getEventoId());
                    return ev.isPresent() && orgId.equals(ev.get().getOrganizacaoResponsavelId());
                })
                .count();

        // Project approvals
        List<ProjetoSolicitacaoAprovacaoEntity> projetoAprovacoesPendentes =
                projetoAprovacaoRepository.findByStatus("PENDENTE");
        long projectApprovalsCount = projetoAprovacoesPendentes.stream()
                .filter(a -> {
                    if (!isFilterNeeded) {
                        return true;
                    }
                    if (a.getProjetoId() == null) {
                        return false;
                    }
                    Optional<ProjetoEventoEntity> pr = projetoRepository.findById(a.getProjetoId());
                    return pr.isPresent() && orgId.equals(pr.get().getOrganizacaoResponsavelId());
                })
                .count();

        int totalSolicitacoesPendentes = (int) (eventApprovalsCount + projectApprovalsCount);

        GeneralSummaryDTO resumoGeral = new GeneralSummaryDTO(
                eventosMes.size(),
                projetosAtivosEntities.size(),
                totalSolicitacoesPendentes
        );

        // 5. Calculate distribution of events by organization
        Map<UUID, String> orgNames = new HashMap<>();
        organizacaoRepository.findAll().forEach(o -> orgNames.put(o.getId(), o.getNome()));

        Map<UUID, Long> countByOrg = eventosMes.stream()
                .collect(Collectors.groupingBy(EventoEntity::getOrganizacaoResponsavelId, Collectors.counting()));

        List<EventDistributionDTO> eventosPorOrganizacao = countByOrg.entrySet().stream()
                .map(entry -> new EventDistributionDTO(
                        orgNames.getOrDefault(entry.getKey(), "Organização Desconhecida"),
                        entry.getValue()
                ))
                .sorted(Comparator.comparingLong(EventDistributionDTO::quantidadeEventos).reversed())
                .toList();

        // 6. Build active projects details
        List<ProjectDetailDTO> projetosAtivosDetalhes = projetosAtivosEntities.stream()
                .map(p -> {
                    int totalEvents = (int) eventoRepository.countByProjetoId(p.getId());
                    int eventsDone = (int) eventoRepository.countByProjetoIdAndFimUtcLessThan(p.getId(), agora);
                    int eventsPending =
                            (int) eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(p.getId(), agora);

                    double percent = projetoAgregacaoDomainService.calcularPercentualTempoDecorrido(p.getInicioUtc(),
                            p.getFimUtc());
                    boolean emRisco =
                            projetoAgregacaoDomainService.verificarSeEstaEmRisco(p.getInicioUtc(), p.getFimUtc(),
                                    eventsPending);

                    List<String> involvedOrgs = eventoRepository.findInvolvedOrganizationNames(p.getId());

                    return new ProjectDetailDTO(
                            p.getNome(),
                            p.getStatus(),
                            percent,
                            totalEvents,
                            eventsDone,
                            emRisco,
                            involvedOrgs
                    );
                })
                .toList();

        // 7. Fetch events for the current week (Monday to Sunday)
        // Find Monday and Sunday of this week
        ZonedDateTime startOfWeek =
                nowZoned.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime endOfWeek =
                nowZoned.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).truncatedTo(ChronoUnit.DAYS);
        Instant inicioSemana = startOfWeek.toInstant();
        Instant fimSemana = endOfWeek.toInstant();

        List<EventoEntity> eventosSemanaEntities = eventoRepository.findAllInPeriod(inicioSemana, fimSemana);
        if (isFilterNeeded) {
            eventosSemanaEntities = eventosSemanaEntities.stream()
                    .filter(e -> orgId.equals(e.getOrganizacaoResponsavelId()))
                    .toList();
        }

        Map<UUID, String> localNames = new HashMap<>();
        localRepository.findAll().forEach(l -> localNames.put(l.getId(), l.getNome()));

        List<WeeklyEventDTO> eventosSemana = eventosSemanaEntities.stream()
                .map(e -> new WeeklyEventDTO(
                        e.getTitulo(),
                        LocalDateTime.ofInstant(e.getInicioUtc(), ZoneId.systemDefault()),
                        localNames.getOrDefault(e.getLocalId(), "Sem Local"),
                        orgNames.getOrDefault(e.getOrganizacaoResponsavelId(), "Organização Desconhecida")
                ))
                .toList();

        return new DashboardMetricsDTO(
                resumoGeral,
                eventosPorOrganizacao,
                projetosAtivosDetalhes,
                eventosSemana
        );
    }
}
