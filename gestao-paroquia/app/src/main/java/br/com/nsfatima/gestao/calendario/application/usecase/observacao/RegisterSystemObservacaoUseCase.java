package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.domain.service.ObservacaoSystemAuthorPolicyService;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterSystemObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final ObservacaoSystemAuthorPolicyService observacaoSystemAuthorPolicyService;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;

    public RegisterSystemObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            ObservacaoSystemAuthorPolicyService observacaoSystemAuthorPolicyService,
            ObservacaoAuditPublisher observacaoAuditPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.observacaoSystemAuthorPolicyService = observacaoSystemAuthorPolicyService;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
    }

    /**
     * Automates the creation of system-level observations (e.g., cancellation reasons) to ensure that important lifecycle changes are always documented.
     * 
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, TipoObservacaoInput.SISTEMA, "Auto-log", null, "system", "cancellation-flow");
     * }
     */
    @Transactional
    public void execute(
            UUID eventoId,
            TipoObservacaoInput tipo,
            String conteudo,
            UUID actorUserId,
            String actor,
            String originFlow) {
        UUID autorFinal = observacaoSystemAuthorPolicyService.resolveAuthorId(actorUserId);

        ObservacaoEventoEntity observacao = new ObservacaoEventoEntity();
        observacao.setId(UUID.randomUUID());
        observacao.setEventoId(eventoId);
        observacao.setUsuarioId(autorFinal);
        observacao.setTipo(tipo.name());
        observacao.setConteudo(conteudo);
        observacao.setCriadoEmUtc(Instant.now());
        observacao.setRemovida(false);
        observacaoEventoJpaRepository.save(observacao);

        observacaoAuditPublisher.publishSystem(
                actor == null || actor.isBlank() ? "system" : actor,
                eventoId.toString(),
                "success",
                Map.of(
                        "observacaoId", observacao.getId().toString(),
                        "eventoId", eventoId,
                        "tipo", tipo.name(),
                        "originFlow", originFlow,
                        "authorSource", actorUserId == null ? "technical-fallback" : "human-actor"));
    }
}
