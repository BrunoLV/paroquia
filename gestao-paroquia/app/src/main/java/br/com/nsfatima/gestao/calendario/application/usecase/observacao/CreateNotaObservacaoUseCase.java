package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.service.ObservacaoMutationPolicyService;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateNotaObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final ObservacaoMutationPolicyService observacaoMutationPolicyService;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;

    public CreateNotaObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            ObservacaoMutationPolicyService observacaoMutationPolicyService,
            ObservacaoAuditPublisher observacaoAuditPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.observacaoMutationPolicyService = observacaoMutationPolicyService;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
    }

    /**
     * Persists a manual user-generated observation, ensuring that the observation type is valid for manual entry and that the action is properly audited.
     * 
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, usuarioId, "actor-name", TipoObservacaoInput.NOTA, "Conteúdo");
     * }
     */
    @Transactional
    public ObservacaoResponse execute(
            UUID eventoId,
            UUID usuarioId,
            String actor,
            TipoObservacaoInput tipo,
            String conteudo) {
        observacaoMutationPolicyService.assertManualCreationAllowed(tipo);

        ObservacaoEventoEntity entity = new ObservacaoEventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setUsuarioId(usuarioId);
        entity.setTipo(tipo.name());
        entity.setConteudo(conteudo);
        entity.setCriadoEmUtc(Instant.now());
        entity.setRemovida(false);

        ObservacaoEventoEntity saved = observacaoEventoJpaRepository.save(entity);
        observacaoAuditPublisher.publishCreate(
                actor,
                eventoId.toString(),
                "success",
                Map.of(
                        "observacaoId", saved.getId().toString(),
                        "eventoId", eventoId,
                        "tipo", tipo.name()));
        return new ObservacaoResponse(
                saved.getId(),
                saved.getEventoId(),
                saved.getUsuarioId(),
                TipoObservacaoResponse.fromInput(tipo),
                saved.getConteudo(),
                saved.getCriadoEmUtc());
    }
}
