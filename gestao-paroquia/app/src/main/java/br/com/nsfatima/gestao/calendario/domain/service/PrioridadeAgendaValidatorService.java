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

/**
 * Serviço de domínio que aplica a hierarquia de prioridade da agenda eclesial entre
 * eventos sobrepostos no tempo. A hierarquia (nível 1 = maior prioridade, nível 4 =
 * menor) é: Nível 1 — Solenidade Global, não pode coexistir com outro Nível 1 e
 * cancela automaticamente qualquer evento de menor prioridade em qualquer
 * igreja/comunidade; Nível 2 — Litúrgico Territorial, não pode sobrepor Nível 1 em
 * lugar nenhum nem outro Nível 2 no mesmo espaço/comunidade, e cancela
 * automaticamente eventos de Nível 3/4 no mesmo espaço/comunidade; Nível 3 — Ação
 * Orgânica Regular, não pode sobrepor Nível 1 em lugar nenhum, nem qualquer evento
 * no mesmo espaço/comunidade; Nível 4 — Atividade de Movimento, não pode sobrepor
 * nenhum evento de prioridade igual ou maior no mesmo espaço/comunidade, nem Nível
 * 1/2/3 em qualquer lugar. Violações lançam {@link ConflitoAgendaEclesialException};
 * quando a regra prevê cancelamento automático em vez de rejeição, os eventos
 * afetados são apenas coletados aqui — o cancelamento efetivo é aplicado por
 * {@link #processAutoCancellation}.
 */
@Service
public class PrioridadeAgendaValidatorService {

    private final EventoJpaRepository eventoJpaRepository;

    public PrioridadeAgendaValidatorService(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    /**
     * Verifica a sobreposição temporal do novo evento contra eventos existentes e
     * aplica a hierarquia de prioridade da agenda eclesial: rejeita a operação com
     * {@link ConflitoAgendaEclesialException} quando o novo evento não pode
     * coexistir com um evento existente, ou coleta (sem aplicar) os eventos de menor
     * prioridade que devem ser cancelados automaticamente como consequência da
     * criação deste evento.
     *
     * @param categoria categoria do novo evento, cujo nível de prioridade rege a validação; {@code null} pula a
     *     validação
     * @param inicioUtc início do intervalo do novo evento, em UTC
     * @param fimUtc fim do intervalo do novo evento, em UTC
     * @param localId identificador do local físico do novo evento, usado para detectar mesmo espaço
     * @param igrejaVinculoId identificador da igreja/comunidade vinculada ao novo evento, usado para detectar mesmo
     *     espaço
     * @param currentEventoId identificador do próprio evento (para excluí-lo da busca de sobreposição em
     *     atualizações), ou {@code null} em criação
     * @param bypass se {@code true}, ignora completamente a validação de prioridade/conflito
     * @return lista de eventos existentes que devem ser cancelados automaticamente em decorrência
     *     da prioridade do novo evento; vazia se nenhum
     * @throws ConflitoAgendaEclesialException se o novo evento sobrepõe um evento existente com o qual não pode
     *     coexistir segundo a hierarquia de prioridade
     */
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
        List<EventoEntity> overlappingEvents =
                eventoJpaRepository.findOverlappingEvents(inicioUtc, fimUtc, currentEventoId);

        List<EventoEntity> eventsToAutoCancel = new ArrayList<>();

        for (EventoEntity existing : overlappingEvents) {
            CategoriaEvento existingCat = existing.getCategoria();
            int existingPriority = existingCat != null ? existingCat.getNivelPrioridade() : 4;

            boolean sameLocationOrChurch = (localId != null && Objects.equals(localId, existing.getLocalId())) ||
                    (igrejaVinculoId != null && Objects.equals(igrejaVinculoId, existing.getIgrejaVinculoId()));

            if (newPriority == 1) {
                // Nível 1 (Solenidade Global): cancelam automaticamente eventos de menor
                // prioridade (2, 3, 4) em todas as igrejas/comunidades
                if (existingPriority > 1) {
                    eventsToAutoCancel.add(existing);
                } else if (existingPriority == 1) {
                    throw new ConflitoAgendaEclesialException(
                            "Conflito de Agenda Eclesial: Já existe uma"
                            + " Solenidade Global (Nível 1) cadastrada para o mesmo período");
                }
            } else if (newPriority == 2) {
                // Nível 2 (Litúrgico Territorial): não pode conflitar com Nível 1
                // (em lugar nenhum) ou outro Nível 2 na mesma igreja/comunidade/local
                if (existingPriority == 1) {
                    throw new ConflitoAgendaEclesialException(
                            "Conflito de Agenda Eclesial: Atividades de Nível 2"
                            + " não podem sobrepor uma Solenidade Global (Nível 1)");
                }
                if (existingPriority == 2 && sameLocationOrChurch) {
                    throw new ConflitoAgendaEclesialException(
                            "Conflito de Agenda Eclesial: Já existe um evento"
                            + " Litúrgico (Nível 2) no mesmo espaço/comunidade");
                }
                if (existingPriority > 2 && sameLocationOrChurch) {
                    // Atividades de menor prioridade (3 e 4) na mesma comunidade/local
                    // são bloqueadas/canceladas pelo evento litúrgico Nível 2
                    eventsToAutoCancel.add(existing);
                }
            } else if (newPriority == 3) {
                // Nível 3 (Ação Orgânica Regular): não pode sobrepor Nível 1
                // (em lugar nenhum) nem Nível 2/3 no mesmo espaço/comunidade
                if (existingPriority < 3) {
                    throw new ConflitoAgendaEclesialException(
                            "Conflito de Agenda Eclesial: Atividades de Nível 3 não podem"
                            + " sobrepor eventos de maior prioridade (Nível 1 ou 2)");
                }
                if (sameLocationOrChurch) {
                    throw new ConflitoAgendaEclesialException(
                            "Conflito de Agenda Eclesial: Conflito de espaço"
                            + " com atividade de Nível " + existingPriority);
                }
            } else if (newPriority == 4) {
                // Nível 4 (Atividades de Movimentos): rejeitar se houver sobreposição com
                // Nível 1, 2 ou 3 (ou outro Nível 4 no mesmo local)
                if (existingPriority < 4 || sameLocationOrChurch) {
                    throw new ConflitoAgendaEclesialException(
                            "Conflito de Agenda Eclesial: Atividades de Movimento (Nível 4)"
                            + " não podem sobrepor eventos de Nível " + existingPriority);
                }
            }
        }

        return eventsToAutoCancel;
    }

    /**
     * Aplica e persiste o cancelamento automático dos eventos coletados por
     * {@link #validatePriorityAndConflicts} como consequência de um evento de maior
     * prioridade ter sido criado. Não faz nada se a lista estiver vazia ou nula.
     *
     * @param eventsToCancel eventos a serem cancelados automaticamente
     * @param motivo motivo do cancelamento a ser registrado; se ausente, usa uma mensagem padrão referindo o conflito
     *     de prioridade
     */
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
