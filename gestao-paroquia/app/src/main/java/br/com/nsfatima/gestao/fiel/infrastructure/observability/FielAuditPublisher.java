package br.com.nsfatima.gestao.fiel.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Publica a trilha das mutações do cadastro de fiéis sem transportar conteúdo cadastral.
 *
 * <p>Os dados de contato, endereço e a justificativa de análise são PII e, por isso, nunca
 * compõem os metadados enviados à infraestrutura de auditoria.
 */
@Component
public class FielAuditPublisher {

    private static final String RESULT_SUCCESS = "success";
    private final AuditoriaPersistenceService auditoria;

    public FielAuditPublisher(AuditoriaPersistenceService auditoria) {
        this.auditoria = auditoria;
    }

    public void publicarSolicitacaoAjuste(UUID solicitacaoId, UUID fielId, UUID atorId,
                                          UUID unidadeTerritorialId) {
        publicar("criar", solicitacaoId, fielId, atorId, unidadeTerritorialId,
                "SOLICITACAO_AJUSTE_CADASTRAL", RESULT_SUCCESS, Map.of("campos", "CONTATO_ENDERECO"));
    }

    public void publicarDecisaoAjuste(UUID solicitacaoId, UUID fielId, UUID atorId,
                                      UUID unidadeTerritorialId, boolean aprovada) {
        String resultado = aprovada ? "approved" : "rejected";
        publicar("aprovacao-decisao", solicitacaoId, fielId, atorId, unidadeTerritorialId,
                "SOLICITACAO_AJUSTE_CADASTRAL", resultado, Map.of("status", resultado.toUpperCase()));
    }

    public void publicarCriacaoRelacionamento(UUID parId, UUID fielOrigemId, UUID atorId,
                                              UUID unidadeTerritorialId, String tipo) {
        publicar("criar", parId, fielOrigemId, atorId, unidadeTerritorialId,
                "RELACIONAMENTO_FAMILIAR", RESULT_SUCCESS, Map.of("tipo", tipo));
    }

    public void publicarInativacaoRelacionamento(UUID parId, UUID fielOrigemId, UUID atorId,
                                                 UUID unidadeTerritorialId) {
        publicar("cancelar", parId, fielOrigemId, atorId, unidadeTerritorialId,
                "RELACIONAMENTO_FAMILIAR", RESULT_SUCCESS, Map.of("modo", "SOFT_DELETE"));
    }

    private void publicar(String acao, UUID recursoId, UUID fielId, UUID atorId,
                          UUID unidadeTerritorialId, String tipoRecurso, String resultado,
                          Map<String, Object> detalheOperacional) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("resourceType", tipoRecurso);
        metadata.put("resourceId", recursoId.toString());
        metadata.put("fielId", fielId.toString());
        metadata.put("atorUsuarioId", atorId.toString());
        metadata.put("grupoParoquialId", unidadeTerritorialId.toString());
        metadata.putAll(detalheOperacional);
        auditoria.log(atorId.toString(), acao, recursoId.toString(), resultado, metadata);
    }
}
