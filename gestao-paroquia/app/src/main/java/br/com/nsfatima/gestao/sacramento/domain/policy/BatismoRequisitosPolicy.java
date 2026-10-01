package br.com.nsfatima.gestao.sacramento.domain.policy;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;

import java.util.ArrayList;
import java.util.List;

/**
 * Requisitos do Batismo (Cânn. 877–878, 873–874): batizando com local de nascimento, vínculo civil,
 * filiação conforme o estado ({@code estadoFiliacao}: MATRIMONIAL, NAO_CASADOS ou ADOTIVA), no máximo
 * um padrinho e uma madrinha elegíveis.
 */
public final class BatismoRequisitosPolicy implements RequisitosAssentoPolicy {

    @Override
    public TipoSacramento tipo() {
        return TipoSacramento.BATISMO;
    }

    @Override
    public List<String> pendencias(TeorAssento teor) {
        List<String> pendencias = new ArrayList<>(RequisitosComuns.metadadosUniversais(teor));
        if (RequisitosComuns.contar(teor, PapelParticipante.BATIZANDO) != 1) {
            pendencias.add("Deve haver exatamente um batizando");
        }
        teor.participantes().stream()
                .filter(p -> p.papel() == PapelParticipante.BATIZANDO)
                .filter(p -> p.localNascimento() == null || p.localNascimento().isBlank())
                .forEach(p -> pendencias.add("Local de nascimento do batizando é obrigatório"));
        RequisitosComuns.exigirDado(pendencias, teor, "certidaoNascimentoCartorio",
                "Cartório da certidão de nascimento é obrigatório");
        RequisitosComuns.exigirDado(pendencias, teor, "certidaoNascimentoTermo",
                "Termo da certidão de nascimento é obrigatório");
        RequisitosComuns.exigirMinistro(pendencias, teor, PapelParticipante.MINISTRO_CELEBRANTE,
                "Ministro celebrante é obrigatório");
        filiacao(pendencias, teor);
        if (RequisitosComuns.contar(teor, PapelParticipante.PADRINHO) > 1
                || RequisitosComuns.contar(teor, PapelParticipante.MADRINHA) > 1) {
            pendencias.add("No máximo um padrinho e uma madrinha (Cân. 873)");
        }
        RequisitosComuns.elegibilidadePadrinhos(pendencias, teor);
        return pendencias;
    }

    private static void filiacao(List<String> pendencias, TeorAssento teor) {
        String estado = teor.dadosEspecificos().getOrDefault("estadoFiliacao", "");
        switch (estado) {
            case "MATRIMONIAL" -> RequisitosComuns.exigirMinimo(pendencias, teor, 1,
                    "Filiação matrimonial exige nome do pai ou da mãe", PapelParticipante.PAI, PapelParticipante.MAE);
            case "NAO_CASADOS" -> {
                RequisitosComuns.exigirMinimo(pendencias, teor, 1,
                        "Filiação de pais não casados exige o nome da mãe (Cân. 877 §2)", PapelParticipante.MAE);
                teor.participantes().stream()
                        .filter(p -> p.papel() == PapelParticipante.PAI)
                        .filter(p -> p.documento() == null || p.documento().isBlank())
                        .forEach(p -> pendencias.add(
                                "Nome do pai só mediante documento autêntico ou declaração perante o pároco "
                                        + "(Cân. 877 §2)"));
            }
            case "ADOTIVA" -> RequisitosComuns.exigirMinimo(pendencias, teor, 1,
                    "Filiação adotiva exige o nome dos adotantes", PapelParticipante.ADOTANTE);
            default -> pendencias.add("Estado de filiação deve ser MATRIMONIAL, NAO_CASADOS ou ADOTIVA");
        }
    }
}
