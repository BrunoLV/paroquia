package br.com.nsfatima.gestao.aprovacao.application.port.out;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;

import java.util.Map;

public interface ApprovalAuditPublisher {
    void publishApprovalDecision(String actor, AprovacaoEntity aprovacao, String outcome, Map<String, Object> metadata);
}
