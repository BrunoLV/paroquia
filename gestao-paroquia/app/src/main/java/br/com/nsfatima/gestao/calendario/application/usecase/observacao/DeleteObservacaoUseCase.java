package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.domain.service.ObservacaoMutationPolicyService;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final ObservacaoMutationPolicyService observacaoMutationPolicyService;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;

    public DeleteObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            ObservacaoMutationPolicyService observacaoMutationPolicyService,
            ObservacaoAuditPublisher observacaoAuditPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.observacaoMutationPolicyService = observacaoMutationPolicyService;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
    }

    /**
     * Marks an observation as removed in the database, enforcing ownership rules to ensure only authors or authorized personnel can hide specific notes.
     * 
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, observacaoId, usuarioId, "admin");
     * }
     */
    @Transactional
    public void execute(UUID eventoId, UUID observacaoId, UUID usuarioId, String actor) {
        ObservacaoEventoEntity observacao = observacaoEventoJpaRepository
                .findByIdAndEventoId(observacaoId, eventoId)
                .orElseThrow(() -> new ObservacaoNaoEncontradaException("Observation not found"));

        observacaoMutationPolicyService.assertCanEditOrDelete(observacao, usuarioId);

        observacao.setRemovida(true);
        observacao.setRemovidaEmUtc(Instant.now());
        observacao.setRemovidaPorUsuarioId(usuarioId);
        observacaoEventoJpaRepository.save(observacao);
        observacaoAuditPublisher.publishDelete(
                actor,
                eventoId.toString(),
                "success",
                Map.of(
                        "observacaoId", observacaoId.toString(),
                        "eventoId", eventoId,
                        "removidaPorUsuarioId", usuarioId));
    }
}
