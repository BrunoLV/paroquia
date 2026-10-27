package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.SolicitacaoAjusteClericalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.SolicitarAjusteClericalRequest;
import br.com.nsfatima.gestao.clero.infrastructure.observability.ClerigoAuditPublisher;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.SolicitacaoAjusteClericalEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.SolicitacaoAjusteClericalJpaRepository;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orquestra solicitações de ajuste cadastral submetidas pelo próprio clérigo e sua moderação. */
@Service
public class GerenciarAjusteClericalUseCase {

    private static final String STATUS_PENDENTE = "PENDENTE";

    private final UsuarioJpaRepository usuarios;
    private final ClerigoJpaRepository clerigos;
    private final SolicitacaoAjusteClericalJpaRepository ajustes;
    private final GerenciarNotificacaoUseCase notificacoes;
    private final ClerigoAuditPublisher auditoria;

    public GerenciarAjusteClericalUseCase(
            UsuarioJpaRepository usuarios,
            ClerigoJpaRepository clerigos,
            SolicitacaoAjusteClericalJpaRepository ajustes,
            GerenciarNotificacaoUseCase notificacoes,
            ClerigoAuditPublisher auditoria) {
        this.usuarios = usuarios;
        this.clerigos = clerigos;
        this.ajustes = ajustes;
        this.notificacoes = notificacoes;
        this.auditoria = auditoria;
    }

    @Transactional
    public UUID solicitar(UUID usuarioId, SolicitarAjusteClericalRequest request) {
        UsuarioEntity usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário solicitante não encontrado."));
        if (usuario.getClerigoId() == null) {
            throw new IllegalStateException("Usuário autenticado não está vinculado a um perfil clerical.");
        }
        ClerigoEntity clerigo = clerigos.findById(usuario.getClerigoId())
                .filter(ClerigoEntity::isAtivo)
                .orElseThrow(() -> new IllegalArgumentException("Clérigo associado não está ativo."));

        SolicitacaoAjusteClericalEntity entity = new SolicitacaoAjusteClericalEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioSolicitanteId(usuarioId);
        entity.setClerigoId(clerigo.getId());
        entity.setEmail(request.email());
        entity.setCelular(request.celular());
        entity.setTelefoneFixo(request.telefoneFixo());
        entity.setEndereco(request.endereco());
        entity.setStatus(STATUS_PENDENTE);

        ajustes.save(entity);
        auditoria.publicarSolicitacaoAjuste(entity.getId(), clerigo.getId(), usuarioId);
        return entity.getId();
    }

    @Transactional
    public void decidir(UUID solicitacaoId, UUID usuarioAprovadorId, boolean aprovada, String justificativa) {
        SolicitacaoAjusteClericalEntity entity = ajustes.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação de ajuste não encontrada."));

        if (!STATUS_PENDENTE.equals(entity.getStatus())) {
            throw new IllegalStateException("Solicitação de ajuste já foi processada.");
        }

        Instant agora = Instant.now();
        entity.setAnalisadoPorUsuarioId(usuarioAprovadorId);
        entity.setAnalisadoEmUtc(agora);

        if (aprovada) {
            entity.setStatus("APROVADA");
            ClerigoEntity clerigo = clerigos.findById(entity.getClerigoId())
                    .orElseThrow(() -> new IllegalArgumentException("Clérigo não encontrado para aplicar ajuste."));
            clerigo.setEmail(entity.getEmail());
            clerigo.setCelular(entity.getCelular());
            if (entity.getTelefoneFixo() != null) {
                clerigo.setTelefoneFixo(entity.getTelefoneFixo());
            }
            if (entity.getEndereco() != null) {
                clerigo.setEndereco(entity.getEndereco());
            }
            clerigos.save(clerigo);
            notificacoes.publicar(
                    entity.getUsuarioSolicitanteId(),
                    "Sua solicitação de ajuste cadastral foi aprovada.");
        } else {
            entity.setStatus("REJEITADA");
            entity.setJustificativaAnalise(justificativa);
            notificacoes.publicar(
                    entity.getUsuarioSolicitanteId(),
                    "Sua solicitação de ajuste cadastral foi rejeitada.");
        }

        ajustes.save(entity);
        auditoria.publicarDecisaoAjuste(entity.getId(), entity.getClerigoId(), usuarioAprovadorId, aprovada);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAjusteClericalResponse> listarPendentes() {
        return ajustes.findByStatusOrderByCriadoEmUtcAsc(STATUS_PENDENTE).stream()
                .map(this::toResponse)
                .toList();
    }

    private SolicitacaoAjusteClericalResponse toResponse(SolicitacaoAjusteClericalEntity e) {
        return new SolicitacaoAjusteClericalResponse(
                e.getId(),
                e.getClerigoId(),
                e.getStatus(),
                e.getEmail(),
                e.getCelular(),
                e.getTelefoneFixo(),
                e.getEndereco(),
                e.getCriadoEmUtc());
    }
}
