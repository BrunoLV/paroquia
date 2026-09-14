package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoNaoEncontradaException;
import br.com.nsfatima.gestao.calendario.domain.policy.MutacaoObservacaoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ObservacaoRegistrada;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Inativa ou remove logicamente notas de colaboração registradas em eventos.
 */
@Service
public class ExcluirObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final MutacaoObservacaoPolicy mutacaoObservacaoPolicy;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;

    public ExcluirObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            MutacaoObservacaoPolicy mutacaoObservacaoPolicy,
            ObservacaoAuditPublisher observacaoAuditPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.mutacaoObservacaoPolicy = mutacaoObservacaoPolicy;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
    }

    /**
     * Marca uma observação como removida no banco de dados, aplicando regras de propriedade para garantir que
     * apenas o autor ou pessoal autorizado possa ocultar notas específicas.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.execute(eventoId, observacaoId, usuarioId, "admin");
     * }
     */
    @Transactional
    public void execute(UUID eventoId, UUID observacaoId, UUID usuarioId, String actor) {
        ObservacaoEventoEntity observacao = observacaoEventoJpaRepository
                .findByIdAndEventoId(observacaoId, eventoId)
                .orElseThrow(() -> new ObservacaoNaoEncontradaException("Observation not found"));

        mutacaoObservacaoPolicy.assertEdicaoOuExclusaoPermitida(
                new ObservacaoRegistrada(observacao.getTipo(), observacao.isRemovida(), observacao.getUsuarioId()),
                usuarioId);

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
