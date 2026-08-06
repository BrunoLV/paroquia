package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.UpdateFielRequest;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateFielUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;

    public UpdateFielUseCase(FielJpaRepository fielRepository, FielMapper fielMapper) {
        this.fielRepository = fielRepository;
        this.fielMapper = fielMapper;
    }

    @Transactional
    public FielResponse execute(UUID id, UpdateFielRequest request, String actor) {
        FielEntity entity = fielRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + id));

        if (request.nome() != null) entity.setNome(request.nome());
        if (request.cpf() != null) entity.setCpf(request.cpf());
        if (request.dataNascimento() != null) entity.setDataNascimento(request.dataNascimento());
        if (request.email() != null) entity.setEmail(request.email());
        if (request.telefone() != null) entity.setTelefone(request.telefone());
        if (request.endereco() != null) entity.setEndereco(request.endereco());
        if (request.vocacao() != null) entity.setVocacao(request.vocacao());
        if (request.ativo() != null) entity.setAtivo(request.ativo());
        if (request.batizado() != null) entity.setBatizado(request.batizado());
        if (request.primeiraComunhao() != null) entity.setPrimeiraComunhao(request.primeiraComunhao());
        if (request.crismado() != null) entity.setCrismado(request.crismado());
        if (request.casadoIgrejaCatolica() != null) entity.setCasadoIgrejaCatolica(request.casadoIgrejaCatolica());

        entity.setAtualizadoEmUtc(Instant.now());
        entity.setAtualizadoPor(actor != null ? actor : "system");

        FielEntity saved = fielRepository.save(entity);
        return fielMapper.toResponse(saved);
    }
}
