package br.com.nsfatima.gestao.sacramento.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Publica no log de auditoria global apenas identificadores opacos das ações sacramentais. O
 * conteúdo canônico (nomes, documentos, valores) nunca é enviado: pertence à trilha do assento.
 */
@Component
public class SacramentoAuditPublisher {

    private static final String TYPE_ASSENTO = "ASSENTO_SACRAMENTAL";

    private final AuditoriaPersistenceService auditoria;

    public SacramentoAuditPublisher(AuditoriaPersistenceService auditoria) {
        this.auditoria = auditoria;
    }

    /**
     * Registra a ação bem-sucedida sobre o assento.
     *
     * @param actor login do autor
     * @param acao verbo da ação
     * @param assentoId assento afetado
     * @param tipoSacramento tipo do sacramento, sem dados pessoais
     */
    public void publish(String actor, String acao, UUID assentoId, String tipoSacramento) {
        auditoria.log(actor, acao, assentoId.toString(), "success", Map.of(
                "resourceType", TYPE_ASSENTO, "resourceId", assentoId.toString(), "tipo", tipoSacramento));
    }
}
