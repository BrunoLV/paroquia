package br.com.nsfatima.gestao.sacramento.domain.policy;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;

import java.util.ArrayList;
import java.util.List;

/**
 * Requisitos da Confirmação (Cânn. 895–896, 893): crismando com referência canônica ao assento de
 * batismo, um único padrinho elegível e ministro com faculdade de crismar.
 */
public final class ConfirmacaoRequisitosPolicy implements RequisitosAssentoPolicy {

    @Override
    public TipoSacramento tipo() {
        return TipoSacramento.CONFIRMACAO;
    }

    @Override
    public List<String> pendencias(TeorAssento teor) {
        List<String> pendencias = new ArrayList<>(RequisitosComuns.metadadosUniversais(teor));
        if (RequisitosComuns.contar(teor, PapelParticipante.CRISMANDO) != 1) {
            pendencias.add("Deve haver exatamente um crismando");
        }
        for (String chave : List.of("batismoParoquia", "batismoDiocese", "batismoLivro", "batismoFolha",
                "batismoTermo")) {
            RequisitosComuns.exigirDado(pendencias, teor, chave, "Referência ao assento de batismo incompleta: "
                    + chave);
        }
        RequisitosComuns.exigirMinistro(pendencias, teor, PapelParticipante.MINISTRO_CELEBRANTE,
                "Ministro da crisma é obrigatório");
        if (RequisitosComuns.contar(teor, PapelParticipante.PADRINHO, PapelParticipante.MADRINHA) > 1) {
            pendencias.add("Um único padrinho canônico (Cân. 893)");
        }
        RequisitosComuns.elegibilidadePadrinhos(pendencias, teor);
        return pendencias;
    }
}
