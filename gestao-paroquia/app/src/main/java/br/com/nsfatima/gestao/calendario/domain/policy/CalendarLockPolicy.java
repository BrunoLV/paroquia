package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalendarLockedException;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CalendarLockPolicy {

    private final AnoParoquialJpaRepository repository;

    public CalendarLockPolicy(AnoParoquialJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Valida se a data de um evento está em um ano paroquial que já foi fechado.
     * Se o ano estiver fechado, somente eventos com status ADICIONADO_EXTRA são permitidos.
     * 
     * Usage Example:
     * policy.checkLock(evento.getInicio(), EventoStatusInput.CONFIRMADO);
     */
    public void checkLock(Instant eventDate, EventoStatusInput status) {
        if (eventDate == null) {
            return;
        }

        int ano = eventDate.atZone(ZoneOffset.UTC).getYear();
        Optional<AnoParoquialEntity> anoParoquial = repository.findByAno(ano);

        if (anoParoquial.isPresent() && 
            AnoParoquialStatus.FECHADO.name().equals(anoParoquial.get().getStatus()) &&
            status != EventoStatusInput.ADICIONADO_EXTRA) {
            throw new CalendarLockedException(ano);
        }
    }
}
