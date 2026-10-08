package br.com.nsfatima.gestao.calendario.infrastructure.scheduling;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoLocalEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoLocalJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Job agendado que gera proativamente todas as instâncias de eventos recorrentes para o ano civil
 * completo (365/366 dias), materializando as linhas correspondentes na tabela de eventos.
 */
@Component
public class GeradorRecorrenciaAnualJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeradorRecorrenciaAnualJob.class);

    private final EventoRecorrenciaJpaRepository recorrenciaRepository;
    private final EventoJpaRepository eventoRepository;
    private final JobLockJpaRepository lockRepository;
    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoLocalJpaRepository eventoLocalJpaRepository;

    public GeradorRecorrenciaAnualJob(
            EventoRecorrenciaJpaRepository recorrenciaRepository,
            EventoJpaRepository eventoRepository,
            JobLockJpaRepository lockRepository,
            AprovacaoJpaRepository aprovacaoRepository) {
        this(recorrenciaRepository, eventoRepository, lockRepository, aprovacaoRepository, null);
    }

    @Autowired
    public GeradorRecorrenciaAnualJob(
            EventoRecorrenciaJpaRepository recorrenciaRepository,
            EventoJpaRepository eventoRepository,
            JobLockJpaRepository lockRepository,
            AprovacaoJpaRepository aprovacaoRepository,
            @Autowired(required = false) EventoLocalJpaRepository eventoLocalJpaRepository) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.eventoRepository = eventoRepository;
        this.lockRepository = lockRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.eventoLocalJpaRepository = eventoLocalJpaRepository;
    }

    /**
     * Executa a geração de ocorrências para o ano corrente. Agendado para rodar todo 1º de
     * janeiro à 01:00. Adquire um lock distribuído antes de executar para evitar processamento
     * duplicado caso mais de uma instância da aplicação esteja rodando; se o lock não puder ser
     * adquirido (já em execução em outra instância), a execução é ignorada silenciosamente. O
     * lock é sempre liberado ao final, mesmo em caso de falha.
     */
    @Scheduled(cron = "0 0 1 1 1 *")
    @Transactional
    public void execute() {
        Instant now = Instant.now();
        if (!lockRepository.acquireLock("YEARLY_RECURRENCE_GENERATOR", now.plus(Duration.ofHours(1)), now)) {
            LOGGER.info("Yearly recurrence generation already running or locked by another instance.");
            return;
        }

        try {
            int year = LocalDate.now(ZoneOffset.UTC).getYear();
            executeForYear(year);
        } finally {
            lockRepository.releaseLock("YEARLY_RECURRENCE_GENERATOR");
        }
    }

    /**
     * Gera as instâncias de evento recorrente para um ano específico, percorrendo todas as regras
     * de recorrência cadastradas e materializando, para cada uma, as datas de ocorrência que caem
     * dentro do ano informado.
     *
     * <p>Exemplo de uso: {@code job.executeForYear(2026);}
     *
     * @param year ano para o qual as ocorrências recorrentes devem ser geradas
     */
    @Transactional
    public void executeForYear(int year) {
        LOGGER.info("Starting yearly recurrence generation for year {}", year);
        List<EventoRecorrenciaEntity> rules = recorrenciaRepository.findAll();

        for (EventoRecorrenciaEntity rule : rules) {
            processRule(rule, year);
        }
        LOGGER.info("Finished yearly recurrence generation for year {}", year);
    }

    private void processRule(EventoRecorrenciaEntity ruleEntity, int year) {
        eventoRepository.findById(ruleEntity.getEventoBaseId()).ifPresent(baseEvent -> {
            LocalDate startOfRecurrence = baseEvent.getInicioUtc().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            List<LocalDate> dates = ruleEntity.getRegra().gerarDatas(startOfRecurrence, endOfYear);

            for (LocalDate date : dates) {
                // Skip the date of the base event to avoid duplication (the base event IS the first occurrence)
                if (date.equals(startOfRecurrence)) {
                    continue;
                }

                if (date.getYear() == year) {
                    generateInstance(baseEvent, date, ruleEntity);
                }
            }
        });
    }

    private void generateInstance(EventoEntity baseEvent, LocalDate date, EventoRecorrenciaEntity ruleEntity) {
        Instant startUtc;
        Instant endUtc;
        if (baseEvent.getTipoHorario() != null && baseEvent.getTipoHorario().isPredefinido()) {
            var faixa = baseEvent.getTipoHorario().calcularIntervalo(date, ZoneId.of("America/Sao_Paulo"));
            startUtc = faixa.inicioUtc();
            endUtc = faixa.fimUtc();
        } else {
            Duration duration = Duration.between(baseEvent.getInicioUtc(), baseEvent.getFimUtc());
            Instant baseDateStart = baseEvent.getInicioUtc().atZone(ZoneOffset.UTC).toLocalDate()
                    .atStartOfDay().toInstant(ZoneOffset.UTC);
            Duration timeOfDay = Duration.between(baseDateStart, baseEvent.getInicioUtc());
            startUtc = date.atStartOfDay().toInstant(ZoneOffset.UTC).plus(timeOfDay);
            endUtc = startUtc.plus(duration);
        }

        // Robust de-duplication check: same series and same start time
        if (eventoRepository.existsByRecorrenciaIdAndInicioUtc(ruleEntity.getId(), startUtc)) {
            LOGGER.debug("Instance for {} at {} already exists in series {}, skipping",
                    baseEvent.getTitulo(), startUtc, ruleEntity.getId());
            return;
        }

        // Verifica se há aprovação para esta ocorrência específica ou se o evento base está pendente
        if (aprovacaoRepository != null) {
            List<AprovacaoEntity> aprovacoesOcorrencia = aprovacaoRepository.findByEventoId(baseEvent.getId());
            boolean ocorrenciaNaoAprovada = aprovacoesOcorrencia.stream()
                    .filter(a -> startUtc.equals(a.getDataOcorrenciaUtc()))
                    .anyMatch(a -> a.getStatus() != AprovacaoStatus.APROVADA);
            if (ocorrenciaNaoAprovada) {
                LOGGER.debug("Ocorrência em {} não aprovada (pendente ou reprovada), pulando materialização", startUtc);
                return;
            }

            boolean basePendente = aprovacoesOcorrencia.stream()
                    .anyMatch(a -> a.getAprovacaoPaiId() == null
                            && TipoSolicitacaoInput.CRIACAO_EVENTO.name().equals(a.getTipoSolicitacao())
                            && a.getStatus() == AprovacaoStatus.PENDENTE);
            if (basePendente) {
                LOGGER.debug("Evento base {} com aprovação pendente, pulando materialização de ocorrências",
                        baseEvent.getId());
                return;
            }
        }

        EventoEntity instance = new EventoEntity();
        instance.setId(UUID.randomUUID());
        instance.setTitulo(baseEvent.getTitulo());
        instance.setDescricao(baseEvent.getDescricao());
        instance.setCategoria(baseEvent.getCategoria());
        instance.setLocalId(baseEvent.getLocalId());
        instance.setProjetoId(baseEvent.getProjetoId());
        instance.setOrganizacaoResponsavelId(baseEvent.getOrganizacaoResponsavelId());
        instance.setStatus(baseEvent.getStatus());
        instance.setInicioUtc(startUtc);
        instance.setFimUtc(endUtc);
        instance.setRecorrenciaId(ruleEntity.getId());
        instance.setTipoHorario(baseEvent.getTipoHorario());
        instance.setFormato(baseEvent.getFormato());

        EventoEntity savedInstance = eventoRepository.save(instance);
        if (eventoLocalJpaRepository != null) {
            List<UUID> localIds = baseEvent.getLocalIds();
            if (localIds == null || localIds.isEmpty()) {
                localIds = eventoLocalJpaRepository.findByEventoId(baseEvent.getId())
                        .stream()
                        .map(EventoLocalEntity::getLocalId)
                        .toList();
            }
            if (!localIds.isEmpty()) {
                List<EventoLocalEntity> novosLocais = localIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .map(locId -> new EventoLocalEntity(savedInstance.getId(), locId))
                        .toList();
                eventoLocalJpaRepository.saveAll(novosLocais);
            } else if (savedInstance.getLocalId() != null) {
                eventoLocalJpaRepository.save(new EventoLocalEntity(savedInstance.getId(), savedInstance.getLocalId()));
            }
        }
    }
}
