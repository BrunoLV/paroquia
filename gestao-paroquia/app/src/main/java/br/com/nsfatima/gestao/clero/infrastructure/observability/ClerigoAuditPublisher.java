package br.com.nsfatima.gestao.clero.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Audita o ciclo clerical com IDs e categorias, nunca valores cadastrais ou justificativas. */
@Component
public class ClerigoAuditPublisher {
    private final AuditoriaPersistenceService auditoria;

    public ClerigoAuditPublisher(AuditoriaPersistenceService auditoria) {
        this.auditoria = auditoria;
    }

    public void publicarSolicitacaoAjuste(UUID solicitacaoId, UUID clerigoId, UUID atorId) {
        publicar("criar", solicitacaoId, clerigoId, atorId, "PENDENTE");
    }

    public void publicarDecisaoAjuste(UUID solicitacaoId, UUID clerigoId, UUID atorId, boolean aprovada) {
        publicar("aprovacao-decisao", solicitacaoId, clerigoId, atorId, aprovada ? "APROVADO" : "REJEITADO");
    }

    public void publicarInativacao(UUID clerigoId, UUID atorId) {
        publicar("cancelar", clerigoId, clerigoId, atorId, "SOFT_DELETE");
    }

    private void publicar(String acao, UUID recursoId, UUID clerigoId, UUID atorId, String status) {
        auditoria.log(atorId.toString(), acao, recursoId.toString(), "success", Map.of(
                "resourceType", "CLERIGO",
                "resourceId", recursoId.toString(),
                "contextId", clerigoId.toString(),
                "atorUsuarioId", atorId.toString(),
                "status", status));
    }
}
