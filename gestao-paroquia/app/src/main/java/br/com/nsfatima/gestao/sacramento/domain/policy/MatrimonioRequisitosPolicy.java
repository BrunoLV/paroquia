package br.com.nsfatima.gestao.sacramento.domain.policy;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;

import java.util.ArrayList;
import java.util.List;

/**
 * Requisitos do Matrimônio (Cânn. 1121–1123): dois cônjuges, ao menos duas testemunhas, ministro
 * assistente e número do processo de habilitação.
 */
public final class MatrimonioRequisitosPolicy implements RequisitosAssentoPolicy {

    @Override
    public TipoSacramento tipo() {
        return TipoSacramento.MATRIMONIO;
    }

    @Override
    public List<String> pendencias(TeorAssento teor) {
        List<String> pendencias = new ArrayList<>(RequisitosComuns.metadadosUniversais(teor));
        if (RequisitosComuns.contar(teor, PapelParticipante.CONJUGE) != 2) {
            pendencias.add("Devem constar exatamente dois cônjuges");
        }
        RequisitosComuns.exigirMinimo(pendencias, teor, 2, "Ao menos duas testemunhas (Cân. 1108)",
                PapelParticipante.TESTEMUNHA);
        RequisitosComuns.exigirMinistro(pendencias, teor, PapelParticipante.MINISTRO_ASSISTENTE,
                "Ministro assistente é obrigatório");
        RequisitosComuns.exigirDado(pendencias, teor, "numeroProcessoHabilitacao",
                "Número do processo de habilitação é obrigatório");
        return pendencias;
    }
}
