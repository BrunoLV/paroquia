package br.com.nsfatima.gestao.calendario.infrastructure.scheduling;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoLocalEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.scheduling.MaterializadorOcorrenciaRecorrencia.ModeloOcorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoLocalJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Job agendado que gera proativamente as instâncias de eventos recorrentes do ano (365/366 dias),
 * materializando as linhas correspondentes na tabela de eventos.
 *
 * <p>Uma ocorrência materializada precisa ser tão real quanto o evento criado pela tela: passa pela
 * detecção de conflito de agenda, herda os locais, os ativos patrimoniais e as organizações
 * envolvidas do evento-base e ocupa os locais/ativos por reserva (ver
 * {@link MaterializadorOcorrenciaRecorrencia}, que dá a cada ocorrência da rotina anual transação própria). Como o job não tem um
 * aprovador à mão, a ocorrência que conflita com a agenda existente <strong>não é materializada</strong>
 * (fica registrada no log como pulada) em vez de nascer ocupando um espaço já tomado.
 *
 * <p>Ano, dia civil e horário local de cada ocorrência são resolvidos no fuso da paróquia
 * ({@link RelogioNegocio}); o cron também dispara nesse fuso.
 */
@Component
public class GeradorRecorrenciaAnualJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeradorRecorrenciaAnualJob.class);

    private final EventoRecorrenciaJpaRepository recorrenciaRepository;
    private final EventoJpaRepository eventoRepository;
    private final JobLockJpaRepository lockRepository;
    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoLocalJpaRepository eventoLocalJpaRepository;
    private final EventoEnvolvidoJpaRepository envolvidoRepository;
    private final ReservaRepository reservaRepository;
    private final MaterializadorOcorrenciaRecorrencia materializador;
    private final RelogioNegocio relogio;

    public GeradorRecorrenciaAnualJob(
            EventoRecorrenciaJpaRepository recorrenciaRepository,
            EventoJpaRepository eventoRepository,
            JobLockJpaRepository lockRepository,
            AprovacaoJpaRepository aprovacaoRepository,
            EventoLocalJpaRepository eventoLocalJpaRepository,
            EventoEnvolvidoJpaRepository envolvidoRepository,
            ReservaRepository reservaRepository,
            MaterializadorOcorrenciaRecorrencia materializador,
            RelogioNegocio relogio) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.eventoRepository = eventoRepository;
        this.lockRepository = lockRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.eventoLocalJpaRepository = eventoLocalJpaRepository;
        this.envolvidoRepository = envolvidoRepository;
        this.reservaRepository = reservaRepository;
        this.materializador = materializador;
        this.relogio = relogio;
    }

    /**
     * Executa a geração de ocorrências para o ano corrente da paróquia. Agendado para rodar todo 1º de
     * janeiro à 01:00 no fuso da paróquia. Adquire um lock distribuído antes de executar para evitar
     * processamento duplicado caso mais de uma instância da aplicação esteja rodando; se o lock não
     * puder ser adquirido (já em execução em outra instância), a execução é ignorada. O lock é sempre
     * liberado ao final, mesmo em caso de falha.
     */
    @Scheduled(cron = "0 0 1 1 1 *", zone = "${app.timezone.output:America/Sao_Paulo}")
    @Transactional
    public void execute() {
        Instant now = relogio.agora();
        if (!lockRepository.acquireLock("YEARLY_RECURRENCE_GENERATOR", now.plus(Duration.ofHours(1)), now)) {
            LOGGER.info("Yearly recurrence generation already running or locked by another instance.");
            return;
        }

        try {
            executeForYear(relogio.anoDe(now));
        } finally {
            lockRepository.releaseLock("YEARLY_RECURRENCE_GENERATOR");
        }
    }

    /**
     * Gera as instâncias de evento recorrente para um ano específico, percorrendo todas as regras
     * de recorrência cadastradas. Cada ocorrência é gravada em transação própria: a falha de uma (por
     * exemplo, reserva tomada por corrida) é registrada em log e as demais seguem.
     *
     * <p>Exemplo de uso: {@code job.executeForYear(2026);}
     *
     * @param year ano para o qual as ocorrências recorrentes devem ser geradas
     */
    @Transactional
    public void executeForYear(int year) {
        LOGGER.info("Starting yearly recurrence generation for year {}", year);
        for (EventoRecorrenciaEntity rule : recorrenciaRepository.findAll()) {
            eventoRepository.findById(rule.getEventoBaseId())
                    .ifPresent(base -> materializarSerie(base, rule, year, true));
        }
        LOGGER.info("Finished yearly recurrence generation for year {}", year);
    }

    /**
     * Gera as ocorrências de uma única regra de recorrência para o ano informado. É o ponto de
     * entrada usado quando uma série acaba de ser criada: apenas a regra afetada é processada, sem
     * revisitar todas as séries cadastradas. Roda na transação do chamador (a série ainda não foi
     * confirmada no banco), de modo que um conflito é erro para o usuário e desfaz a criação inteira.
     *
     * @param rule regra (série) a materializar
     * @param year ano civil, no fuso da paróquia, a gerar
     */
    @Transactional
    public void executeForRule(EventoRecorrenciaEntity rule, int year) {
        eventoRepository.findById(rule.getEventoBaseId()).ifPresent(base -> materializarSerie(base, rule, year, false));
    }

    private void materializarSerie(EventoEntity base, EventoRecorrenciaEntity rule, int year, boolean isolar) {
        List<AprovacaoEntity> aprovacoes = aprovacaoRepository.findByEventoId(base.getId());
        if (baseAguardandoAprovacao(aprovacoes)) {
            LOGGER.debug("Evento base {} com aprovação pendente, pulando materialização de ocorrências",
                    base.getId());
            return;
        }

        LocalDate inicioDaSerie = relogio.dataDe(base.getInicioUtc());
        List<LocalDate> datas = rule.getRegra().gerarDatas(inicioDaSerie, LocalDate.of(year, 12, 31));
        Set<Instant> iniciosExistentes = new HashSet<>();
        eventoRepository.findByRecorrenciaId(rule.getId()).forEach(e -> iniciosExistentes.add(e.getInicioUtc()));
        ModeloOcorrencia modelo = carregarModelo(base);

        for (LocalDate data : datas) {
            // O evento-base já é a primeira ocorrência da série.
            if (!data.equals(inicioDaSerie) && data.getYear() == year) {
                Intervalo ocorrencia = calcularIntervalo(base, data);
                if (!iniciosExistentes.contains(ocorrencia.inicio())
                        && !ocorrenciaNaoAprovada(aprovacoes, ocorrencia.inicio())
                        && materializar(modelo, rule, ocorrencia, isolar)) {
                    iniciosExistentes.add(ocorrencia.inicio());
                }
            }
        }
    }

    /** Devolve {@code true} apenas se a ocorrência foi gravada; com {@code isolar}, o erro é só registrado. */
    private boolean materializar(ModeloOcorrencia modelo, EventoRecorrenciaEntity rule, Intervalo o, boolean isolar) {
        if (!isolar) {
            return materializador.materializar(modelo, rule, o.inicio(), o.fim());
        }
        try {
            return materializador.materializarIsolada(modelo, rule, o.inicio(), o.fim());
        } catch (RuntimeException e) {
            LOGGER.warn("Falha ao materializar ocorrência de '{}' em {} na série {}; seguindo com as demais: {}",
                    modelo.base().getTitulo(), o.inicio(), rule.getId(), e.getMessage());
            return false;
        }
    }

    private Intervalo calcularIntervalo(EventoEntity base, LocalDate data) {
        if (base.getTipoHorario() != null && base.getTipoHorario().isPredefinido()) {
            var faixa = base.getTipoHorario().calcularIntervalo(data, relogio.fuso());
            return new Intervalo(faixa.inicioUtc(), faixa.fimUtc());
        }
        Instant inicio = relogio.mesmoHorarioNoDia(base.getInicioUtc(), data);
        return new Intervalo(inicio, inicio.plus(Duration.between(base.getInicioUtc(), base.getFimUtc())));
    }

    private boolean baseAguardandoAprovacao(List<AprovacaoEntity> aprovacoes) {
        return aprovacoes.stream()
                .anyMatch(a -> a.getAprovacaoPaiId() == null
                        && TipoSolicitacaoInput.CRIACAO_EVENTO.name().equals(a.getTipoSolicitacao())
                        && a.getStatus() == AprovacaoStatus.PENDENTE);
    }

    private boolean ocorrenciaNaoAprovada(List<AprovacaoEntity> aprovacoes, Instant inicio) {
        boolean pulada = aprovacoes.stream()
                .filter(a -> inicio.equals(a.getDataOcorrenciaUtc()))
                .anyMatch(a -> a.getStatus() != AprovacaoStatus.APROVADA);
        if (pulada) {
            LOGGER.debug("Ocorrência em {} não aprovada (pendente ou reprovada), pulando materialização", inicio);
        }
        return pulada;
    }

    private ModeloOcorrencia carregarModelo(EventoEntity base) {
        List<UUID> localIds = base.getLocalIds();
        if (localIds == null || localIds.isEmpty()) {
            localIds = eventoLocalJpaRepository.findByEventoId(base.getId()).stream()
                    .map(EventoLocalEntity::getLocalId)
                    .toList();
        }
        List<UUID> locais = localIds.stream().filter(Objects::nonNull).distinct().toList();
        List<UUID> ativos = reservaRepository.findByEventoId(base.getId()).stream()
                .filter(r -> r.getTipoItem() == TipoItemReserva.ATIVO && r.getStatus().ocupaAgenda())
                .map(Reserva::getItemId)
                .distinct()
                .toList();
        return new ModeloOcorrencia(base, locais, ativos, envolvidoRepository.findByEventoId(base.getId()));
    }

    private record Intervalo(Instant inicio, Instant fim) {
    }
}
