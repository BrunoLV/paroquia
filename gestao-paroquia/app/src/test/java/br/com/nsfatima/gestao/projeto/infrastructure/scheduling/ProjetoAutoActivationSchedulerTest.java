package br.com.nsfatima.gestao.projeto.infrastructure.scheduling;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

class ProjetoAutoActivationSchedulerTest {

    private ProjetoEventoJpaRepository repository;
    private JobLockJpaRepository lockRepository;
    private ProjetoAutoActivationScheduler scheduler;

    @BeforeEach
    void setUp() {
        repository = mock(ProjetoEventoJpaRepository.class);
        lockRepository = mock(JobLockJpaRepository.class);
        scheduler = new ProjetoAutoActivationScheduler(repository, lockRepository);
    }

    @Test
    void deveAtivarPendenteProjetosQuandoLockAcquired() {
        when(lockRepository.acquireLock(anyString(), any(), any())).thenReturn(true);

        ProjetoEventoEntity project = new ProjetoEventoEntity();
        project.setId(UUID.randomUUID());
        project.setNome("Projeto Teste");
        project.setStatus(ProjetoStatus.ABERTO);

        when(repository.findByStatusAndInicioUtcLessThanEqual(eq(ProjetoStatus.ABERTO), any()))
                .thenReturn(List.of(project));

        scheduler.activateProjects();

        assertEquals(ProjetoStatus.EM_ANDAMENTO, project.getStatus());
        verify(repository, times(1)).save(project);
        verify(lockRepository, times(1)).releaseLock(eq("PROJECT_AUTO_ACTIVATION_JOB"));
    }

    @Test
    void deveNaoAtivarQuandoLockAcquisitionFalha() {
        when(lockRepository.acquireLock(anyString(), any(), any())).thenReturn(false);

        scheduler.activateProjects();

        verify(repository, never()).findByStatusAndInicioUtcLessThanEqual(any(), any());
        verify(lockRepository, never()).releaseLock(anyString());
    }
}
