package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoNaoEncontradaException;
import br.com.nsfatima.gestao.calendario.domain.policy.MutacaoObservacaoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ObservacaoRegistrada;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoNotaRevisaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoNotaRevisaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Atualiza o conteúdo descritivo de uma anotação existente associada a um evento.
 */
@Service
public class AtualizarObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final ObservacaoNotaRevisaoJpaRepository observacaoNotaRevisaoJpaRepository;
    private final MutacaoObservacaoPolicy mutacaoObservacaoPolicy;
    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;

    public AtualizarObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            ObservacaoNotaRevisaoJpaRepository observacaoNotaRevisaoJpaRepository,
            MutacaoObservacaoPolicy mutacaoObservacaoPolicy,
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            ObservacaoAuditPublisher observacaoAuditPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.observacaoNotaRevisaoJpaRepository = observacaoNotaRevisaoJpaRepository;
        this.mutacaoObservacaoPolicy = mutacaoObservacaoPolicy;
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
    }

    /**
     * Atualiza uma observação existente mantendo um histórico de revisões, permitindo correções sem perder o
     * rastro de auditoria das mudanças de informação.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.execute(eventoId, observacaoId, usuarioId, "author", "New content");
     * }
     */
    @Transactional
    public ObservacaoResponse execute(UUID eventoId, UUID observacaoId, UUID usuarioId, String actor,
                                      String novoConteudo) {
        ObservacaoEventoEntity observacao = observacaoEventoJpaRepository
                .findByIdAndEventoId(observacaoId, eventoId)
                .orElseThrow(() -> new ObservacaoNaoEncontradaException("Observation not found"));

        mutacaoObservacaoPolicy.assertEdicaoOuExclusaoPermitida(
                new ObservacaoRegistrada(observacao.getTipo(), observacao.isRemovida(), observacao.getUsuarioId()),
                usuarioId);

        ObservacaoNotaRevisaoEntity revisao = new ObservacaoNotaRevisaoEntity();
        revisao.setId(UUID.randomUUID());
        revisao.setObservacaoId(observacao.getId());
        revisao.setConteudoAnterior(observacao.getConteudo());
        revisao.setConteudoNovo(novoConteudo);
        revisao.setRevisadoPorUsuarioId(usuarioId);
        revisao.setRevisadoEmUtc(Instant.now());
        observacaoNotaRevisaoJpaRepository.save(revisao);

        observacao.setConteudo(novoConteudo);
        ObservacaoEventoEntity saved = observacaoEventoJpaRepository.save(observacao);
        observacaoAuditPublisher.publishUpdate(
                actor,
                eventoId.toString(),
                "success",
                Map.of(
                        "observacaoId", observacaoId.toString(),
                        "eventoId", eventoId,
                        "revisadoPorUsuarioId", usuarioId));

        return new ObservacaoResponse(
                saved.getId(),
                saved.getEventoId(),
                saved.getUsuarioId(),
                TipoObservacaoResponse.fromStoredValue(
                        saved.getTipo(),
                        legacyEnumInconsistencyPublisher,
                        saved.getEventoId().toString()),
                saved.getConteudo(),
                saved.getCriadoEmUtc());
    }
}
