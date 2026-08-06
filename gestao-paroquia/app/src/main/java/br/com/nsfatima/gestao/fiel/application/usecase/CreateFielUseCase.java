package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateFielUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;

    public CreateFielUseCase(FielJpaRepository fielRepository, FielMapper fielMapper) {
        this.fielRepository = fielRepository;
        this.fielMapper = fielMapper;
    }

    @Transactional
    public FielResponse execute(CreateFielRequest request, String actor) {
        FielEntity entity = new FielEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(request.nome());
        entity.setCpf(request.cpf());
        entity.setDataNascimento(request.dataNascimento());
        entity.setEmail(request.email());
        entity.setTelefone(request.telefone());
        entity.setEndereco(request.endereco());
        entity.setVocacao(request.vocacao());
        entity.setAtivo(true);
        entity.setBatizado(Boolean.TRUE.equals(request.batizado()));
        entity.setPrimeiraComunhao(Boolean.TRUE.equals(request.primeiraComunhao()));
        entity.setCrismado(Boolean.TRUE.equals(request.crismado()));
        entity.setCasadoIgrejaCatolica(Boolean.TRUE.equals(request.casadoIgrejaCatolica()));

        Instant now = Instant.now();
        entity.setCriadoEmUtc(now);
        entity.setAtualizadoEmUtc(now);
        entity.setCriadoPor(actor != null ? actor : "system");
        entity.setAtualizadoPor(actor != null ? actor : "system");

        FielEntity saved = fielRepository.save(entity);
        return fielMapper.toResponse(saved);
    }
}
