package br.com.nsfatima.gestao.sacramento.domain.policy;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Requisitos da Ordem Sacra (Cânn. 1053–1054): ordenado, bispo ordenante, grau conferido e
 * diocese ou congregação de incardinação.
 */
public final class OrdemRequisitosPolicy implements RequisitosAssentoPolicy {

    private static final Set<String> GRAUS = Set.of("DIACONATO_PERMANENTE", "DIACONATO_TRANSITORIO", "PRESBITERADO");

    @Override
    public TipoSacramento tipo() {
        return TipoSacramento.ORDEM;
    }

    @Override
    public List<String> pendencias(TeorAssento teor) {
        List<String> pendencias = new ArrayList<>(RequisitosComuns.metadadosUniversais(teor));
        if (RequisitosComuns.contar(teor, PapelParticipante.ORDENADO) != 1) {
            pendencias.add("Deve haver exatamente um ordenado");
        }
        if (RequisitosComuns.contar(teor, PapelParticipante.BISPO_ORDENANTE) != 1) {
            pendencias.add("Deve constar o bispo ordenante");
        }
        if (!GRAUS.contains(teor.dadosEspecificos().getOrDefault("grau", ""))) {
            pendencias.add("Grau deve ser DIACONATO_PERMANENTE, DIACONATO_TRANSITORIO ou PRESBITERADO");
        }
        RequisitosComuns.exigirDado(pendencias, teor, "incardinacao",
                "Diocese ou congregação de incardinação é obrigatória");
        return pendencias;
    }
}
