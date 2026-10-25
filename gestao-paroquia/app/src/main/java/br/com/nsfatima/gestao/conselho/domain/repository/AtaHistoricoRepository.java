package br.com.nsfatima.gestao.conselho.domain.repository;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.RegistroAuditoriaAta;

import java.util.List;

/** Porta append-only da trilha de auditoria das atas: inclui e consulta, nunca altera ou remove. */
public interface AtaHistoricoRepository {

    void registrar(RegistroAuditoriaAta registro);

    List<RegistroAuditoriaAta> listarPorAta(AtaReuniaoId ataId);
}
