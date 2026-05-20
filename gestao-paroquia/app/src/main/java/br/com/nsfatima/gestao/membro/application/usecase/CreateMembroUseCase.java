package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateMembroUseCase {

    private final MembroJpaRepository repository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public CreateMembroUseCase(
            MembroJpaRepository repository,
            MembroMapper mapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    /**
     * Persiste um novo registro de membro no banco de dados, inicializando seu status como ativo e registrando a ação para auditoria.
     * 
     * Exemplo: useCase.execute(membroRequest, "system_admin")
     */
    public MembroResponse execute(MembroRequest request, String actor) {
        MembroEntity entity = new MembroEntity();
        entity.setId(UUID.randomUUID());
        entity.setNomeCompleto(request.nomeCompleto());
        entity.setDataNascimento(request.dataNascimento());
        entity.setEmail(request.email());
        entity.setTelefone(request.telefone());
        entity.setEndereco(request.endereco());
        entity.setUsuarioId(request.usuarioId());
        entity.setDataBatismo(request.dataBatismo());
        entity.setLocalBatismo(request.localBatismo());
        entity.setDataCrisma(request.dataCrisma());
        entity.setDataMatrimonio(request.dataMatrimonio());
        entity.setAtivo(true);

        MembroEntity saved = repository.save(entity);
        auditPublisher.publish(actor, "create", saved.getId().toString(), "success");

        return mapper.toResponse(saved);
    }
}
