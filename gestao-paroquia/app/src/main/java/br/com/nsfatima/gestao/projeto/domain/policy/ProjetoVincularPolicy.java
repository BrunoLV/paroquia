package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProjetoVincularPolicy {

    private final ProjetoEventoJpaRepository projetoRepository;

    public ProjetoVincularPolicy(ProjetoEventoJpaRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    /**
     * Validates if an event can be linked to a project.
     * 
     * @param projetoId The ID of the project to link to.
     * @param eventStart The start date of the event.
     * @param eventEnd The end date of the event.
     * @param isRecurring Whether the event is part of a recurrence.
     */
    public void validateLink(UUID projetoId, Instant eventStart, Instant eventEnd, boolean isRecurring) {
        if (projetoId == null) {
            return;
        }

        if (isRecurring) {
            throw new IllegalArgumentException("Recurring events cannot be linked to projects");
        }

        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projetoId));

        if (projeto.getStatusEnum() != ProjetoStatus.ATIVO) {
            throw new IllegalStateException("Cannot link event to an INACTIVE project");
        }

        if (eventStart.isBefore(projeto.getInicioUtc()) || eventEnd.isAfter(projeto.getFimUtc())) {
            throw new IllegalArgumentException("Event dates must be within the project timeframe: " 
                    + projeto.getInicioUtc() + " to " + projeto.getFimUtc());
        }
    }
}
