package br.com.nsfatima.gestao.patrimonio.infrastructure.scheduling;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.patrimonio.application.usecase.SincronizarStatusReservadoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Varredura periódica que reconcilia o status {@code RESERVADO}/{@code DISPONIVEL} de todos os
 * ativos não excluídos e não em manutenção. É o backstop dos casos que não têm transição de estado
 * disparando evento: o início e o fim do período de uma reserva confirmada. Usa lock distribuído
 * para execução única entre instâncias.
 */
@Component
public class SincronizarStatusReservadoJob extends QuartzJobBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SincronizarStatusReservadoJob.class);
    private static final String LOCK_NAME = "PATRIMONIO_STATUS_RESERVADO_JOB";
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);

    private final AtivoPatrimonialRepository ativoRepository;
    private final SincronizarStatusReservadoAtivoUseCase sincronizarUseCase;
    private final JobLockJpaRepository lockRepository;

    public SincronizarStatusReservadoJob(AtivoPatrimonialRepository ativoRepository,
                                         SincronizarStatusReservadoAtivoUseCase sincronizarUseCase,
                                         JobLockJpaRepository lockRepository) {
        this.ativoRepository = ativoRepository;
        this.sincronizarUseCase = sincronizarUseCase;
        this.lockRepository = lockRepository;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Instant agora = Instant.now();
        if (!lockRepository.acquireLock(LOCK_NAME, agora.plus(LOCK_TTL), agora)) {
            LOGGER.debug("Job {} já em execução por outra instância — pulando.", LOCK_NAME);
            return;
        }
        try {
            ativoRepository.findSincronizaveisDeReserva()
                    .forEach(ativo -> sincronizarUseCase.execute(ativo.getId().value()));
        } finally {
            lockRepository.releaseLock(LOCK_NAME);
        }
    }
}
