package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.MutacaoObservacaoPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Insere nota complementar em evento com validação de autoria e papéis operacionais.
 */
@Service
public class CriarNotaObservacaoUseCase {

    private final ObservacaoEventoJpaRepository observacaoEventoJpaRepository;
    private final MutacaoObservacaoPolicy mutacaoObservacaoPolicy;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;

    public CriarNotaObservacaoUseCase(
            ObservacaoEventoJpaRepository observacaoEventoJpaRepository,
            MutacaoObservacaoPolicy mutacaoObservacaoPolicy,
            ObservacaoAuditPublisher observacaoAuditPublisher) {
        this.observacaoEventoJpaRepository = observacaoEventoJpaRepository;
        this.mutacaoObservacaoPolicy = mutacaoObservacaoPolicy;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
    }

    /**
     * Persiste uma observação manual gerada pelo usuário, garantindo que o tipo de observação seja válido para
     * lançamento manual e que a ação seja devidamente auditada.
     * <p>
     * Exemplo de uso:
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
        mutacaoObservacaoPolicy.assertCriacaoManualPermitida(tipo);

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
