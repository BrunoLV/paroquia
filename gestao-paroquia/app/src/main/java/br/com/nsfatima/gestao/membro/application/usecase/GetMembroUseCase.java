package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetMembroUseCase {

    private final MembroJpaRepository repository;
    private final MembroMapper mapper;

    public GetMembroUseCase(MembroJpaRepository repository, MembroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    /**
     * Recupera as informações detalhadas de um membro pelo seu identificador único para fornecer dados de perfil e histórico.
     * 
     * Exemplo: useCase.execute(membroId)
     */
    public MembroResponse execute(UUID id) {
        MembroEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + id));
        return mapper.toResponse(entity);
    }
}
