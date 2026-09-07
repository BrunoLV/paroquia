package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.NotificacaoResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.NotificacaoEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.NotificacaoJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case para gerenciamento de notificações persistentes aos fiéis. */
@Service
public class GerenciarNotificacaoUseCase {

    private final NotificacaoJpaRepository repository;

    public GerenciarNotificacaoUseCase(NotificacaoJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void publicar(UUID usuarioId, String mensagem) {
        var notificacao = new NotificacaoEntity();
        notificacao.setId(UUID.randomUUID());
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo("AJUSTE_CADASTRAL_DECIDIDO");
        notificacao.setMensagem(mensagem);
        notificacao.setCriadoEmUtc(Instant.now());
        repository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponse> listar(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByCriadoEmUtcDesc(usuarioId).stream()
                .map(n -> new NotificacaoResponse(
                        n.getId(),
                        n.getTipo(),
                        n.getMensagem(),
                        n.getCriadoEmUtc(),
                        n.getLidaEmUtc()))
                .toList();
    }

    @Transactional
    public void marcarLida(UUID id, UUID usuarioId) {
        var notificacao = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada."));
        if (notificacao.getLidaEmUtc() == null) {
            notificacao.setLidaEmUtc(Instant.now());
            repository.save(notificacao);
        }
    }
}
