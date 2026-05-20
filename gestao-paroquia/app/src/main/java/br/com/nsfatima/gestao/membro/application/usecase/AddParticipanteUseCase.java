package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.ParticipanteOrganizacaoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.ParticipanteOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddParticipanteUseCase {

    private final ParticipanteOrganizacaoJpaRepository repository;
    private final MembroJpaRepository membroRepository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public AddParticipanteUseCase(
            ParticipanteOrganizacaoJpaRepository repository,
            MembroJpaRepository membroRepository,
            MembroMapper mapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.membroRepository = membroRepository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    /**
     * Registra o ingresso de um membro em uma organização, validando a inexistência de duplicidade ativa para garantir a integridade dos vínculos.
     * 
     * Exemplo: useCase.execute(membroId, request, "admin_user")
     */
    public ParticipacaoResponse execute(UUID membroId, ParticipacaoRequest request, String actor) {
        if (!membroRepository.existsById(membroId)) {
            throw new IllegalArgumentException("Membro nao encontrado: " + membroId);
        }

        // Evitar duplicidade ativa na mesma organização
        boolean alreadyParticipating = repository.findByMembroIdAndAtivoTrue(membroId).stream()
                .anyMatch(p -> p.getOrganizacaoId().equals(request.organizacaoId()));

        if (alreadyParticipating) {
            throw new IllegalArgumentException("Membro já participa ativamente desta organização");
        }

        ParticipanteOrganizacaoEntity entity = new ParticipanteOrganizacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setMembroId(membroId);
        entity.setOrganizacaoId(request.organizacaoId());
        entity.setDataInicio(request.dataInicio());
        entity.setAtivo(true);

        ParticipanteOrganizacaoEntity saved = repository.save(entity);
        auditPublisher.publish(actor, "add-participation", membroId.toString(), "success");

        return mapper.toParticipacaoResponse(saved);
    }
}
