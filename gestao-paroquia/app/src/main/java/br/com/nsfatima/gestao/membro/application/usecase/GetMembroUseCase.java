package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Recupera um vínculo usuário-organização (membro) pelo identificador, para consulta de detalhes.
 */
@Service
public class GetMembroUseCase {

    private final MembroJpaRepository repository;
    private final MembroMapper mapper;

    public GetMembroUseCase(MembroJpaRepository repository, MembroMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Busca o vínculo pelo identificador informado.
     *
     * @param id identificador do vínculo
     * @return o vínculo encontrado, com dados de exibição resolvidos
     * @throws IllegalArgumentException se não houver vínculo com o {@code id} informado
     */
    @Transactional(readOnly = true)
    public MembroResponse execute(UUID id) {
        MembroEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + id));
        return mapper.toResponse(entity);
    }
}
