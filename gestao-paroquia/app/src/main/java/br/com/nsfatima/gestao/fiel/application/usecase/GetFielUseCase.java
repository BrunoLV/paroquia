package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetFielUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;

    public GetFielUseCase(FielJpaRepository fielRepository, FielMapper fielMapper) {
        this.fielRepository = fielRepository;
        this.fielMapper = fielMapper;
    }

    @Transactional(readOnly = true)
    public FielResponse execute(UUID id) {
        return fielRepository.findById(id)
                .map(fielMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));
    }
}
