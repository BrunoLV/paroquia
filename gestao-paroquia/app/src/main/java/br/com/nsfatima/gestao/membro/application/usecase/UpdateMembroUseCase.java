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
public class UpdateMembroUseCase {

    private final MembroJpaRepository repository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public UpdateMembroUseCase(
            MembroJpaRepository repository,
            MembroMapper mapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    /**
     * Atualiza os dados cadastrais e sacramentais de um membro, garantindo que as informações estejam corretas e registrando a alteração para auditoria.
     * 
     * Exemplo: useCase.execute(membroId, membroRequest, "admin_user")
     */
    public MembroResponse execute(UUID id, MembroRequest request, String actor) {
        MembroEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + id));

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

        MembroEntity saved = repository.save(entity);
        auditPublisher.publish(actor, "update", saved.getId().toString(), "success");

        return mapper.toResponse(saved);
    }

    @Transactional
    /**
     * Altera o status de atividade de um membro no sistema para refletir sua situação atual de participação na comunidade paroquial.
     * 
     * Exemplo: useCase.toggleAtivo(membroId, false, "admin_user")
     */
    public void toggleAtivo(UUID id, boolean ativo, String actor) {
        MembroEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + id));
        entity.setAtivo(ativo);
        repository.save(entity);
        auditPublisher.publish(actor, ativo ? "activate" : "deactivate", id.toString(), "success");
    }
}
