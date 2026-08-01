package br.com.nsfatima.gestao.patrimonio.infrastructure.scheduling;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.event.AtivoAlocacaoAtrasoEvent;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
public class AtivoAlocacaoAgendaJob extends QuartzJobBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivoAlocacaoAgendaJob.class);
    private static final String LOCK_NAME = "ATIVO_ALOCACAO_AGENDA_JOB";
    private static final int DIAS_TOLERANCIA_ATRASO = 2;

    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final JobLockJpaRepository lockRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AtivoAlocacaoAgendaJob(AtivoAlocacaoJpaRepository alocacaoRepository,
                                   AtivoPatrimonialJpaRepository ativoRepository,
                                   JobLockJpaRepository lockRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.alocacaoRepository = alocacaoRepository;
        this.ativoRepository = ativoRepository;
        this.lockRepository = lockRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) {
        Instant now = Instant.now();
        if (!lockRepository.acquireLock(LOCK_NAME, now.plus(Duration.ofMinutes(10)), now)) {
            LOGGER.info("Ativo alocação agenda job already running or locked by another instance.");
            return;
        }

        try {
            ativarOcupacoes();
            detectarAtrasos();
        } finally {
            lockRepository.releaseLock(LOCK_NAME);
        }
    }

    private void ativarOcupacoes() {
        LocalDate hoje = LocalDate.now();
        List<AtivoAlocacaoEntity> aIniciar = alocacaoRepository
                .findByStatusAndDataInicioLessThanEqual(AtivoAlocacaoStatus.CONFIRMADA, hoje);

        for (AtivoAlocacaoEntity ocupacao : aIniciar) {
            ativoRepository.findById(ocupacao.getAtivoId())
                    .filter(ativo -> ativo.getStatus() == AtivoStatus.DISPONIVEL)
                    .ifPresent(ativo -> ativarOcupacao(ativo, ocupacao));
        }
    }

    private void ativarOcupacao(AtivoPatrimonialEntity ativo, AtivoAlocacaoEntity ocupacao) {
        ativo.setStatus(ocupacao.getTipo() == AtivoAlocacaoTipo.MANUTENCAO
                ? AtivoStatus.EM_MANUTENCAO
                : AtivoStatus.ALOCADO);
        ativoRepository.save(ativo);
    }

    private void detectarAtrasos() {
        LocalDate limite = LocalDate.now().minusDays(DIAS_TOLERANCIA_ATRASO);
        List<AtivoAlocacaoEntity> atrasadas = alocacaoRepository
                .findByStatusAndDataFimLessThanEqualAndAlertaAtrasoEmitidoEmIsNull(
                        AtivoAlocacaoStatus.CONFIRMADA, limite);

        for (AtivoAlocacaoEntity ocupacao : atrasadas) {
            marcarAtrasada(ocupacao);
            marcarImpactadasPorAtraso(ocupacao);
        }
    }

    private void marcarAtrasada(AtivoAlocacaoEntity ocupacao) {
        ocupacao.setStatus(AtivoAlocacaoStatus.ATRASADA);
        ocupacao.setAlertaAtrasoEmitidoEm(Instant.now());
        alocacaoRepository.save(ocupacao);
        eventPublisher.publishEvent(new AtivoAlocacaoAtrasoEvent(
                ocupacao.getId(), ocupacao.getAtivoId(), ocupacao.getTipo()));
    }

    private void marcarImpactadasPorAtraso(AtivoAlocacaoEntity atrasada) {
        List<AtivoAlocacaoEntity> futuras = alocacaoRepository.findByAtivoIdAndStatusIn(atrasada.getAtivoId(),
                List.of(AtivoAlocacaoStatus.PENDENTE_APROVACAO, AtivoAlocacaoStatus.CONFIRMADA));

        futuras.stream()
                .filter(futura -> futura.getDataInicio().isAfter(atrasada.getDataFim()))
                .forEach(futura -> {
                    futura.setImpactadaPorAtraso(true);
                    alocacaoRepository.save(futura);
                });
    }
}
