package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import br.com.nsfatima.gestao.fiel.domain.model.TipoRelacionamentoFamiliar;
import java.util.UUID;

/** Resposta sem dados civis, válida para qualquer pessoa familiar. */
public record RelacionamentoFamiliarResponse(
        UUID id,
        UUID pessoaId,
        TipoPessoaFamiliar tipoEntidade,
        String nome,
        String relacao,
        TipoRelacionamentoFamiliar tipoRelacaoBase) {
    public UUID fielId() {
        return tipoEntidade == TipoPessoaFamiliar.FIEL ? pessoaId : null;
    }
}
