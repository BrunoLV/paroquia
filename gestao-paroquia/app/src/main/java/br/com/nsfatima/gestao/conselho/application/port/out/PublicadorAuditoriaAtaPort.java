package br.com.nsfatima.gestao.conselho.application.port.out;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;

import java.util.UUID;

/** Porta de saída que replica as ações sobre atas no log de auditoria global, só com identificadores opacos. */
public interface PublicadorAuditoriaAtaPort {

    /**
     * Publica uma ação executada sobre a ata.
     *
     * @param ator login do autor
     * @param usuarioId identificador do usuário autor, ou {@code null}
     * @param acaoAta ação do conselho (RASCUNHAR, HOMOLOGAR, EXPORTAR_PDF etc.)
     * @param ata ata afetada
     */
    void publicar(String ator, UUID usuarioId, String acaoAta, AtaReuniao ata);
}
