package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListMembrosUseCase {

    private final MembroJpaRepository repository;
    private final MembroMapper mapper;

    public ListMembrosUseCase(MembroJpaRepository repository, MembroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    /**
     * Executa a listagem filtrada e paginada de membros para possibilitar a gestão eficiente e organizada do cadastro paroquial.
     * 
     * Exemplo: useCase.execute("Maria", true, pageable)
     */
    public Page<MembroResponse> execute(String nome, Boolean ativo, Pageable pageable) {
        return repository.findByFiltros(nome, ativo, pageable)
                .map(mapper::toResponse);
    }
}
