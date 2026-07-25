package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListMembrosUseCase {

    private final MembroJpaRepository repository;
    private final MembroMapper mapper;

    public ListMembrosUseCase(MembroJpaRepository repository, MembroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<MembroResponse> execute(UUID organizacaoId, UUID usuarioId, Boolean ativo, Pageable pageable) {
        return repository.findByFiltros(organizacaoId, usuarioId, ativo, pageable)
                .map(mapper::toResponse);
    }
}
