package br.com.nsfatima.gestao.projeto.application.dto;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;

public record ProjetoCreationResult(
        ProjetoResponse projeto,
        ProjetoAprovacaoResponse aprovacao
) {
    public boolean isPendingApproval() {
        return aprovacao != null;
    }
}
