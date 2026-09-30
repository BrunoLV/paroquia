package br.com.nsfatima.gestao.governanca.domain.policy;

import br.com.nsfatima.gestao.governanca.domain.exception.AnoParoquialNaoAutorizadoException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;

/**
 * Autoriza exclusivamente o Pároco (e Bispo Diocesano em supervisão) para a trava/abertura anual.
 * O papel técnico ADMIN e os conselhos consultivos são estritamente bloqueados (Cân. 515 §1 e 536 §2).
 */
public final class ConselhoOuParocoAutorizacaoAnoParoquialPolicy implements AutorizacaoAnoParoquialPolicy {

    @Override
    public void assertPodeGerir(String rawRole, String rawTipoGrupo) {
        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(rawRole);
        boolean autorizado = papelGlobal == PapelGlobal.PAROCO || papelGlobal == PapelGlobal.BISPO_DIOCESANO;
        if (!autorizado) {
            throw new AnoParoquialNaoAutorizadoException(
                    "Apenas o Pároco possui autoridade canônica para gerir o Ano Paroquial");
        }
    }
}
