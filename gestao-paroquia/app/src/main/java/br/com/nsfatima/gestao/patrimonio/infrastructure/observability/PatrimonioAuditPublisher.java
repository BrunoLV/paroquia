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
     * @param organizacaoId organização dona do ativo
     */
    public void publishCreateSuccess(String actor, UUID ativoId, UUID organizacaoId) {
        auditLogPersistenceService.log(actor, "criar", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoId, Map.of()));
    }

    /**
     * Registra a auditoria de transferência de organização dona de um ativo patrimonial.
     *
     * @param actor                ator responsável pela ação
     * @param ativoId              ativo transferido
     * @param organizacaoOrigemId organização dona antes da transferência
     * @param organizacaoDestinoId organização dona depois da transferência
     */
    public void publishTransfer(String actor, UUID ativoId, UUID organizacaoOrigemId, UUID organizacaoDestinoId) {
        auditLogPersistenceService.log(actor, "transferir", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoDestinoId, Map.of(
                        "organizacaoOrigemId", organizacaoOrigemId.toString(),
                        "organizacaoDestinoId", organizacaoDestinoId.toString())));
    }

    /**
     * Registra a auditoria de alocação de um ativo patrimonial, incluindo o evento vinculado
     * quando houver.
     *
     * @param actor         ator responsável pela ação
     * @param ativoId       ativo alocado
     * @param organizacaoId organização dona do ativo
     * @param eventoId      evento de destino da alocação, ou {@code null}
     */
    public void publishAlocacao(String actor, UUID ativoId, UUID organizacaoId, UUID eventoId) {
        Map<String, Object> metadata = eventoId == null ? Map.of() : Map.of("eventoId", eventoId.toString());
        auditLogPersistenceService.log(actor, "alocar", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoId, metadata));
    }

    /**
     * Registra a auditoria de desalocação (devolução) de um ativo patrimonial.
     *
     * @param actor         ator responsável pela ação
     * @param ativoId       ativo desalocado
     * @param organizacaoId organização dona do ativo
     */
    public void publishDesalocacao(String actor, UUID ativoId, UUID organizacaoId) {
        auditLogPersistenceService.log(actor, "desalocar", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoId, Map.of()));
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

    private Map<String, Object> withDefaults(UUID ativoId, UUID organizacaoId, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", TYPE_ATIVO);
        resolved.put("resourceId", ativoId.toString());
        if (organizacaoId != null) {
            resolved.put("organizacaoId", organizacaoId.toString());
        }
        resolved.putAll(metadata);
        return resolved;
    }
}
