package br.com.nsfatima.gestao.projeto.application.dto;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;

/**
 * Resultado de uma operação de criação ou cancelamento de projeto que pode ter sido aplicada
 * imediatamente ou desviada para aprovação. Exatamente um dos dois campos é preenchido:
 * {@code projeto} quando a operação foi aplicada diretamente, {@code aprovacao} quando foi
 * criada uma solicitação de aprovação pendente (ator sem nível de autorização suficiente para
 * a ação imediata — ver {@code TransitionProjetoUseCase#cancelar}).
 */
public record ProjetoCreationResult(
        ProjetoResponse projeto,
        ProjetoAprovacaoResponse aprovacao
) {

    /**
     * Indica se a operação foi desviada para uma solicitação de aprovação pendente, em vez de
     * ser aplicada imediatamente.
     *
     * @return {@code true} se há uma solicitação de aprovação pendente associada ao resultado.
     */
    public boolean isPendenteAprovacao() {
        return aprovacao != null;
    }
}
