package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoAutorInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoTipoImutavelException;
import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoTipoManualInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;

import java.util.UUID;

/** Restringe mutações manuais a notas não removidas pelo próprio autor. */
public final class NotaManualMutacaoPolicy implements MutacaoObservacaoPolicy {

    @Override
    public void assertCriacaoManualPermitida(TipoObservacaoInput tipo) {
        if (tipo != TipoObservacaoInput.NOTA) {
            throw new ObservacaoTipoManualInvalidoException("Apenas NOTA é aceita no endpoint manual de observação");
        }
    }

    @Override
    public void assertEdicaoOuExclusaoPermitida(ObservacaoRegistrada observacao, UUID solicitanteId) {
        if (!"NOTA".equalsIgnoreCase(observacao.tipo()) || observacao.removida()) {
            throw new ObservacaoTipoImutavelException("Observação de sistema ou removida é imutável");
        }
        if (!observacao.autorId().equals(solicitanteId)) {
            throw new ObservacaoAutorInvalidoException("Usuário não é o autor da observação");
        }
    }
}
