package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListAprovacoesUseCase {

    private final AprovacaoJpaRepository repository;
    private final AprovacaoMapper mapper;

    public ListAprovacoesUseCase(AprovacaoJpaRepository repository, AprovacaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Retrieves a paginated list of approval requests to support UI components that display governance history and pending tasks.
     * 
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, "PENDENTE", PageRequest.of(0, 20));
     * }
     */
    @Transactional(readOnly = true)
    public Page<AprovacaoResponse> execute(UUID eventoId, String status, Pageable pageable) {
        return repository.findByFilter(eventoId, status, pageable)
                .map(mapper::toResponse);
    }
}
