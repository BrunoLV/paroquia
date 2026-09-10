package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Publica um registro de auditoria toda vez que um valor bruto persistido em um campo de enum
 * legado (tipicamente vindo de dados antigos/importados que precederam a modelagem atual como
 * enum) não corresponde a nenhum valor conhecido pelos mapeamentos de Input/Response da API
 * atual. Isso caracteriza uma inconsistência de dado legado ("legacy enum drift"): o valor
 * persistido no banco não é mais representável pelo enum de domínio corrente, e este publisher
 * existe para tornar esse desvio visível e rastreável (via trilha de auditoria) em vez de falhar
 * silenciosamente ou ser mascarado em uma conversão automática.
 *
 * <p>A ação de auditoria emitida é fixa ({@code "legacy-enum-inconsistency"}), o alvo é a
 * combinação {@code tipoAgregado:idAgregado}, o resultado é sempre {@code "detected"}, e os
 * metadados registrados são o nome do campo afetado ({@code field}) e o valor bruto encontrado
 * ({@code rawValue}), sanitizado (normalização de espaços e truncado a 80 caracteres) para evitar
 * poluir o log/registro de auditoria com valores muito longos ou malformados.
 */
@Component
public class LegacyEnumInconsistencyPublisher {

    private final AuditoriaPersistenceService auditLogPersistenceService;

    public LegacyEnumInconsistencyPublisher(AuditoriaPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    /**
     * Registra, via {@link AuditoriaPersistenceService}, a detecção de um valor bruto persistido
     * que não corresponde a nenhum valor conhecido do enum de domínio esperado para o campo
     * informado.
     *
     * @param aggregateType tipo do agregado onde o valor inconsistente foi encontrado (ex.: nome
     *     da entidade)
     * @param aggregateId identificador do agregado onde o valor inconsistente foi encontrado
     * @param field nome do campo cujo valor persistido não corresponde a um valor conhecido do
     *     enum
     * @param rawValue valor bruto efetivamente persistido no banco, antes de sanitização
     */
    public void publish(String aggregateType, String aggregateId, String field, String rawValue) {
        auditLogPersistenceService.log(
                "system",
                "legacy-enum-inconsistency",
                "%s:%s".formatted(aggregateType, aggregateId),
                "detected",
                Map.of(
                        "field", field,
                        "rawValue", sanitize(rawValue)));
    }

    private String sanitize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "<empty>";
        }

        String normalized = rawValue.replaceAll("\\s+", " ").trim();
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }
}
