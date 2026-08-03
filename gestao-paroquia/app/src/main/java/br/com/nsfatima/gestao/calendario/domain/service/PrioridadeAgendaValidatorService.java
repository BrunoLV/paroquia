package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PrioridadeAgendaValidatorService {

    private final EventoJpaRepository eventoJpaRepository;

    public PrioridadeAgendaValidatorService(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    @Transactional
    public List<EventoEntity> validatePriorityAndConflicts(
            CategoriaEvento categoria,
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            UUID igrejaVinculoId,
            UUID currentEventoId,
            boolean bypass) {

        if (bypass) {
            return List.of();
        }

        if (categoria == null || inicioUtc == null || fimUtc == null) {
            return List.of();
        }

        int newPriority = categoria.getNivelPrioridade();
        List<EventoEntity> overlappingEvents = eventoJpaRepository.findOverlappingEvents(inicioUtc, fimUtc, currentEventoId);

        List<EventoEntity> eventsToAutoCancel = new ArrayList<>();

        for (EventoEntity existing : overlappingEvents) {
            CategoriaEvento existingCat = existing.getCategoria();
            int existingPriority = existingCat != null ? existingCat.getNivelPrioridade() : 4;

            boolean sameLocationOrChurch = (localId != null && Objects.equals(localId, existing.getLocalId())) ||
                    (igrejaVinculoId != null && Objects.equals(igrejaVinculoId, existing.getIgrejaVinculoId()));

            if (newPriority == 1) {
                // Nível 1 (Solenidade Global): Solenidades de Nível 1 cancelam automaticamente todos os eventos de menor prioridade (2, 3, 4) em todas as igrejas/comunidades
                if (existingPriority > 1) {
                    eventsToAutoCancel.add(existing);
                } else if (existingPriority == 1) {
                    throw new ConflitoAgendaEclesialException("Conflito de Agenda Eclesial: Já existe uma Solenidade Global (Nível 1) cadastrada para o mesmo período");
                }
            } else if (newPriority == 2) {
                // Nível 2 (Litúrgico Territorial): Não pode conflitar com Nível 1 (em lugar nenhum) ou outro Nível 2 na mesma igreja/comunidade/local
                if (existingPriority == 1) {
                    throw new ConflitoAgendaEclesialException("Conflito de Agenda Eclesial: Atividades de Nível 2 não podem sobrepor uma Solenidade Global (Nível 1)");
                }
                if (existingPriority == 2 && sameLocationOrChurch) {
                    throw new ConflitoAgendaEclesialException("Conflito de Agenda Eclesial: Já existe um evento Litúrgico (Nível 2) no mesmo espaço/comunidade");
                }
                if (existingPriority > 2 && sameLocationOrChurch) {
                    // Atividades de menor prioridade (3 e 4) na mesma comunidade/local são bloqueadas/canceladas pelo evento litúrgico Nível 2
                    eventsToAutoCancel.add(existing);
                }
            } else if (newPriority == 3) {
                // Nível 3 (Ação Orgânica Regular): Não pode sobrepor Nível 1 (em lugar nenhum) nem Nível 2/3 no mesmo espaço/comunidade
                if (existingPriority < 3) {
                    throw new ConflitoAgendaEclesialException("Conflito de Agenda Eclesial: Atividades de Nível 3 não podem sobrepor eventos de maior prioridade (Nível 1 ou 2)");
                }
                if (sameLocationOrChurch) {
                    throw new ConflitoAgendaEclesialException("Conflito de Agenda Eclesial: Conflito de espaço com atividade de Nível " + existingPriority);
                }
            } else if (newPriority == 4) {
                // Nível 4 (Atividades de Movimentos): Rejeitar se houver sobreposição com Nível 1, 2 ou 3 (ou outro Nível 4 no mesmo local)
                if (existingPriority < 4 || sameLocationOrChurch) {
                    throw new ConflitoAgendaEclesialException("Conflito de Agenda Eclesial: Atividades de Movimento (Nível 4) não podem sobrepor eventos de Nível " + existingPriority);
                }
            }
        }

        return eventsToAutoCancel;
    }

    @Transactional
    public void processAutoCancellation(List<EventoEntity> eventsToCancel, String motivo) {
        if (eventsToCancel == null || eventsToCancel.isEmpty()) {
            return;
        }

        String motivoFinal = (motivo != null && !motivo.isBlank())
                ? motivo
                : "Cancelado automaticamente devido a conflito de prioridade de agenda eclesial";

        for (EventoEntity evento : eventsToCancel) {
            evento.setStatus(EventoStatusInput.CANCELADO.name());
            evento.setCanceladoMotivo(motivoFinal);
        }

        eventoJpaRepository.saveAll(eventsToCancel);
    }
}
