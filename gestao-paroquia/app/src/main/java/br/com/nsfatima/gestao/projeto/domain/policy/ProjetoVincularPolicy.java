package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ProjetoVincularPolicy {

    private final ProjetoEventoJpaRepository projetoRepository;

    public ProjetoVincularPolicy(ProjetoEventoJpaRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    /**
     * Validates if a single event can be linked to a project.
     * 
     * @param projetoId The ID of the project to link to.
     * @param eventStart The start date of the event.
     * @param eventEnd The end date of the event.
     */
    public void validateLink(UUID projetoId, Instant eventStart, Instant eventEnd) {
        if (projetoId == null) {
            return;
        }

        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projetoId));

        if (projeto.getStatusEnum() != ProjetoStatus.ATIVO) {
            throw new IllegalStateException("Cannot link event to an INACTIVE project");
        }

        if (projeto.getInicioUtc() == null || projeto.getFimUtc() == null) {
            return; // Skip boundary validation if project timeframe is not defined
        }

        if (eventStart.isBefore(projeto.getInicioUtc()) || eventEnd.isAfter(projeto.getFimUtc())) {
            throw new IllegalArgumentException("Event dates must be within the project timeframe: " 
                    + projeto.getInicioUtc() + " to " + projeto.getFimUtc());
        }
    }

    /**
     * Validates if a recurring event series can be linked to a project.
     * 
     * @param projetoId The ID of the project to link to.
     * @param firstStart The start date of the first event occurrence.
     * @param firstEnd The end date of the first event occurrence.
     * @param regra The recurrence rule.
     */
    public void validateRecurrence(UUID projetoId, Instant firstStart, Instant firstEnd, br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia regra) {
        if (projetoId == null) {
            return;
        }

        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projetoId));

        if (projeto.getStatusEnum() != ProjetoStatus.ATIVO) {
            throw new IllegalStateException("Cannot link event to an INACTIVE project");
        }

        if (projeto.getInicioUtc() == null || projeto.getFimUtc() == null) {
            return; // Skip boundary validation if project timeframe is not defined
        }

        if (firstStart.isBefore(projeto.getInicioUtc())) {
            throw new IllegalArgumentException("Event start date is before project start date");
        }

        LocalDate endOfProject = projeto.getFimUtc().atZone(java.time.ZoneOffset.UTC).toLocalDate();

        if (regra.dataLimite() == null && regra.numeroOcorrencias() == null) {
            throw new IllegalArgumentException("Recurring event must have an end date or a maximum number of occurrences to fit within the project timeline.");
        }

        if (regra.dataLimite() != null && regra.dataLimite().isAfter(endOfProject)) {
            throw new IllegalArgumentException("Recurrence end date (" + regra.dataLimite() + ") exceeds project end date (" + endOfProject + ").");
        }

        if (regra.numeroOcorrencias() != null) {
            LocalDate startOfSeries = firstStart.atZone(java.time.ZoneOffset.UTC).toLocalDate();
            java.util.List<LocalDate> dates = regra.gerarDatas(startOfSeries, endOfProject.plusYears(5));
            if (dates.isEmpty()) {
                throw new IllegalArgumentException("No occurrences generated for this recurrence rule.");
            }
            LocalDate lastDate = dates.get(Math.min(dates.size(), regra.numeroOcorrencias()) - 1);
            if (lastDate.isAfter(endOfProject)) {
                throw new IllegalArgumentException("Recurrence occurrences exceed project end date (" + endOfProject + ").");
            }
        }
    }
}
