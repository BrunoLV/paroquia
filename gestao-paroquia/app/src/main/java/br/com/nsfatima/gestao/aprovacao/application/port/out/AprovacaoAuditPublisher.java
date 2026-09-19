package br.com.nsfatima.gestao.aprovacao.application.port.out;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;

import java.util.Map;

/**
 * Porta de saída (Ports &amp; Adapters) para registro de auditoria das decisões de aprovação. Isola
 * {@code DecidirSolicitacaoAprovacaoUseCase} do mecanismo concreto de auditoria, garantindo trilha de quem
 * decidiu o quê (executado, reprovado ou falhou) sem acoplar o use case ao formato de persistência do log.
 */
public interface AprovacaoAuditPublisher {

    /**
     * Registra na trilha de auditoria a decisão tomada sobre uma solicitação de aprovação.
     *
     * @param actor      identificador do ator que tomou a decisão
     * @param aprovacao  aprovação decidida
     * @param outcome    resultado da decisão (ex.: "executado", "reprovado", "falhou")
     * @param metadata   metadados adicionais relevantes para a auditoria (ex.: organização, tipo de erro)
     */
    void publishAprovacaoDecision(String actor, AprovacaoEntity aprovacao, String outcome,
            Map<String, Object> metadata);
}
