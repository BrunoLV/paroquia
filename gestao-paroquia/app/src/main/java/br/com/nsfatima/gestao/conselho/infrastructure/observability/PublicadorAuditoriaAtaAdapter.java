package br.com.nsfatima.gestao.conselho.infrastructure.observability;

import br.com.nsfatima.gestao.conselho.application.port.out.PublicadorAuditoriaAtaPort;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Replica as ações sobre atas no log de auditoria global, reutilizando o vocabulário fechado do
 * {@link AuditoriaPersistenceService} (só ele é persistido): criação e retificação são "criar", edição,
 * submissão e reabertura são "atualizar", homologação e veto são "aprovacao-decisao" e a exportação é "ler".
 * Vão apenas identificadores opacos; o conteúdo da ata fica na trilha própria do contexto.
 */
@Component
public class PublicadorAuditoriaAtaAdapter implements PublicadorAuditoriaAtaPort {

    private static final String TIPO_RECURSO = "ATA_REUNIAO";
    private static final String RESULTADO = "success";
    private static final String ACAO_PADRAO = "atualizar";
    private static final String PREFIXO_EXPORTACAO = "EXPORTAR_";
    private static final Map<String, String> ACAO_GLOBAL = Map.of(
            "RASCUNHAR", "criar",
            "RETIFICAR", "criar",
            "HOMOLOGAR", "aprovacao-decisao",
            "VETAR", "aprovacao-decisao");

    private final AuditoriaPersistenceService auditoria;

    public PublicadorAuditoriaAtaAdapter(AuditoriaPersistenceService auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void publicar(String ator, UUID usuarioId, String acaoAta, AtaReuniao ata) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("resourceType", TIPO_RECURSO);
        metadata.put("resourceId", ata.getId().toString());
        metadata.put("organizacaoId", ata.getOrganizacaoConvocanteId().toString());
        metadata.put("acaoAta", acaoAta);
        metadata.put("tipoReuniao", ata.getTipo().name());
        if (usuarioId != null) {
            metadata.put("usuarioId", usuarioId.toString());
        }
        auditoria.log(ator, acaoGlobal(acaoAta), ata.getId().toString(), RESULTADO, metadata);
    }

    private static String acaoGlobal(String acaoAta) {
        if (acaoAta.startsWith(PREFIXO_EXPORTACAO)) {
            return "ler";
        }
        return ACAO_GLOBAL.getOrDefault(acaoAta, ACAO_PADRAO);
    }
}
