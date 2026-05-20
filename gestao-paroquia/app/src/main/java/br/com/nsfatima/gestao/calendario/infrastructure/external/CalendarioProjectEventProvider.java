package br.com.nsfatima.gestao.calendario.infrastructure.external;

import br.com.nsfatima.gestao.projeto.domain.service.ProjectEventProvider;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class CalendarioProjectEventProvider implements ProjectEventProvider {

    private final EventoJpaRepository eventoRepository;

    public CalendarioProjectEventProvider(EventoJpaRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public long countByProjetoId(UUID projetoId) {
        return eventoRepository.countByProjetoId(projetoId);
    }

    @Override
    public long countByProjetoIdAndStatus(UUID projetoId, String status) {
        return eventoRepository.countByProjetoIdAndStatus(projetoId, status);
    }

    @Override
    public long countByProjetoIdAndFimUtcLessThan(UUID projetoId, Instant data) {
        return eventoRepository.countByProjetoIdAndFimUtcLessThan(projetoId, data);
    }

    @Override
    public long countByProjetoIdAndFimUtcGreaterThanEqual(UUID projetoId, Instant data) {
        return eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(projetoId, data);
    }

    @Override
    public List<String> findInvolvedOrganizationNames(UUID projetoId) {
        return eventoRepository.findInvolvedOrganizationNames(projetoId);
    }
}
