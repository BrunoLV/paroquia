package br.com.nsfatima.gestao.patrimonio.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publica entradas de auditoria para as ações mutantes do contexto de patrimônio (criação,
 * transferência, alocação, desalocação, negação de escrita), delegando a persistência ao
 * {@code AuditoriaPersistenceService} comum, com os metadados padronizados por tipo de ação.
 */
@Component
public class PatrimonioAuditPublisher {

    private static final String TYPE_ATIVO = "ATIVO_PATRIMONIAL";
    private static final String RESULT_SUCCESS = "success";

    private final AuditoriaPersistenceService auditLogPersistenceService;

    public PatrimonioAuditPublisher(AuditoriaPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    /**
     * Registra a auditoria de criação bem-sucedida de um ativo patrimonial.
     *
     * @param actor         ator responsável pela ação
     * @param ativoId       ativo criado
     * @param grupoParoquialId organização dona do ativo
     */
    public void publishCreateSuccess(String actor, UUID ativoId, UUID grupoParoquialId) {
        auditLogPersistenceService.log(actor, "criar", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, grupoParoquialId, Map.of()));
    }

    /**
     * Registra a auditoria de envio de um ativo para manutenção.
     *
     * @param actor           ator responsável pela ação
     * @param ativoId         ativo enviado para manutenção
     * @param grupoParoquialId unidade territorial dona do ativo
     */
    public void publishManutencao(String actor, UUID ativoId, UUID grupoParoquialId) {
        auditLogPersistenceService.log(actor, "enviar-manutencao", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, grupoParoquialId, Map.of()));
    }

    /**
     * Registra a auditoria de exclusão lógica de um ativo patrimonial.
     *
     * @param actor           ator responsável pela ação
     * @param ativoId         ativo excluído
     * @param grupoParoquialId unidade territorial dona do ativo
     */
    public void publishExclusao(String actor, UUID ativoId, UUID grupoParoquialId) {
        auditLogPersistenceService.log(actor, "excluir", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, grupoParoquialId, Map.of()));
    }

    /**
     * Registra a auditoria de uma tentativa de escrita negada por falta de autorização.
     *
     * @param actor   ator que teve a ação negada
     * @param ativoId ativo alvo da tentativa, ou {@code null} se não aplicável
     */
    public void publishDeniedWrite(String actor, UUID ativoId) {
        auditLogPersistenceService.log(actor, "escrita-negada", ativoId == null ? "ativo" : ativoId.toString(),
                "ACCESS_DENIED", Map.of("resourceType", TYPE_ATIVO, "errorCategory", "AUTHZ"));
    }

    private Map<String, Object> withDefaults(UUID ativoId, UUID grupoParoquialId, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", TYPE_ATIVO);
        resolved.put("resourceId", ativoId.toString());
        if (grupoParoquialId != null) {
            resolved.put("grupoParoquialId", grupoParoquialId.toString());
        }
        resolved.putAll(metadata);
        return resolved;
    }
}
