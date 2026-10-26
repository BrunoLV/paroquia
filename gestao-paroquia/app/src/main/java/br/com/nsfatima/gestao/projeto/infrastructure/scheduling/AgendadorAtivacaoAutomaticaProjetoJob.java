package br.com.nsfatima.gestao.projeto.infrastructure.scheduling;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.Projeto;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
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
 * quando ninguém aciona {@code TransicionarProjetoUseCase#iniciar} manualmente. A transição passa
 * pelo agregado {@link Projeto#iniciar()}, que é quem guarda a máquina de estados (RN-PROJ-01). Usa
 * {@code JobLockJpaRepository} para garantir execução exclusiva entre instâncias da aplicação e
 * dispara no fuso da paróquia, para que "o início chegou" seja a virada do dia local.
 */
@Component
public class AgendadorAtivacaoAutomaticaProjetoJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgendadorAtivacaoAutomaticaProjetoJob.class);
    private static final String LOCK_NAME = "PROJECT_AUTO_ACTIVATION_JOB";

    private final ProjetoEventoJpaRepository repository;
    private final JobLockJpaRepository lockRepository;
    private final ProjetoMapper mapper;
    private final RelogioNegocio relogio;

    public AgendadorAtivacaoAutomaticaProjetoJob(
            ProjetoEventoJpaRepository repository,
            JobLockJpaRepository lockRepository,
            ProjetoMapper mapper,
            RelogioNegocio relogio) {
        this.repository = repository;
        this.lockRepository = lockRepository;
        this.mapper = mapper;
        this.relogio = relogio;
    }

    /**
     * Executa diariamente (00:00:01 no fuso da paróquia) e ativa (muda para {@code EM_ANDAMENTO}) todos os
     * projetos {@code ABERTO} cuja data de início já foi atingida. Adquire um lock nomeado antes
     * de rodar para evitar execução concorrente em múltiplas instâncias da aplicação; se o lock
     * não puder ser adquirido, a execução é ignorada silenciosamente (apenas logada).
     */
    @Scheduled(cron = "1 0 0 * * *", zone = "${app.timezone.output:America/Sao_Paulo}")
    @Transactional
    public void activateProjects() {
        Instant now = relogio.agora();
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
                    Projeto agregado = mapper.toDomain(project);
                    agregado.iniciar();
                    mapper.aplicar(agregado, project);
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
