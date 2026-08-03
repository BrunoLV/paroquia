package br.com.nsfatima.gestao.projeto.infrastructure.scheduling;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Job agendado que automatiza a transição {@code ABERTO -> EM_ANDAMENTO} de projetos cuja data
 * de início já foi atingida, evitando que projetos fiquem indefinidamente em {@code ABERTO}
 * quando ninguém aciona {@code TransitionProjetoUseCase#iniciar} manualmente. Usa
 * {@code JobLockJpaRepository} para garantir execução exclusiva entre instâncias da aplicação.
 */
@Component
public class ProjetoAutoActivationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjetoAutoActivationScheduler.class);
    private static final String LOCK_NAME = "PROJECT_AUTO_ACTIVATION_JOB";

    private final ProjetoEventoJpaRepository repository;
    private final JobLockJpaRepository lockRepository;

    public ProjetoAutoActivationScheduler(
            ProjetoEventoJpaRepository repository,
            JobLockJpaRepository lockRepository) {
        this.repository = repository;
        this.lockRepository = lockRepository;
    }

    /**
     * Executa diariamente (00:00:01 UTC) e ativa (muda para {@code EM_ANDAMENTO}) todos os
     * projetos {@code ABERTO} cuja data de início já foi atingida. Adquire um lock nomeado antes
     * de rodar para evitar execução concorrente em múltiplas instâncias da aplicação; se o lock
     * não puder ser adquirido, a execução é ignorada silenciosamente (apenas logada).
     */
    @Scheduled(cron = "1 0 0 * * *", zone = "UTC")
    @Transactional
    public void activateProjects() {
        Instant now = Instant.now();
        if (!lockRepository.acquireLock(LOCK_NAME, now.plus(Duration.ofMinutes(10)), now)) {
            LOGGER.info("Project auto-activation job already running or locked by another instance.");
            return;
        }

        try {
            LOGGER.info("Starting automatic project activation check...");
            List<ProjetoEventoEntity> projectsToActivate = repository.findByStatusAndInicioUtcLessThanEqual(
                    ProjetoStatus.ABERTO, now);

            if (!projectsToActivate.isEmpty()) {
                LOGGER.info("Found {} project(s) to activate.", projectsToActivate.size());
                for (ProjetoEventoEntity project : projectsToActivate) {
                    project.setStatus(ProjetoStatus.EM_ANDAMENTO);
                    repository.save(project);
                    LOGGER.info("Project '{}' ({}) activated successfully.", project.getNome(), project.getId());
                }
            } else {
                LOGGER.info("No projects found to activate.");
            }
        } finally {
            lockRepository.releaseLock(LOCK_NAME);
        }
    }
}
