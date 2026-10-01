package br.com.nsfatima.gestao.sacramento.domain.policy;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Matriz de alçadas do Cân. 535 §1: a custódia e a fé pública são privativas do Pároco; a
 * secretaria exerce escrituração preparatória. {@code ADMIN} nunca atua em atos de fé pública
 * (RN-IAM-04) e livro reservado é vedado à secretaria, aberto apenas a Pároco e Bispo Diocesano.
 * Exclusão física não existe como operação.
 */
public final class AlcadaSacramentalPolicy {

    private static final Set<PapelGlobal> PAROCO = Set.of(PapelGlobal.PAROCO);
    private static final Set<PapelGlobal> PAROCO_E_SECRETARIA = Set.of(
            PapelGlobal.PAROCO, PapelGlobal.SECRETARIA_PAROQUIAL);
    private static final Set<PapelGlobal> LEITURA = Set.of(
            PapelGlobal.PAROCO, PapelGlobal.SECRETARIA_PAROQUIAL, PapelGlobal.BISPO_DIOCESANO);
    private static final Set<PapelGlobal> LIVRO_RESERVADO = Set.of(PapelGlobal.PAROCO, PapelGlobal.BISPO_DIOCESANO);

    private static final Map<OperacaoSacramental, Set<PapelGlobal>> MATRIZ = new EnumMap<>(OperacaoSacramental.class);

    static {
        MATRIZ.put(OperacaoSacramental.LER, LEITURA);
        MATRIZ.put(OperacaoSacramental.LAVRAR_RASCUNHO, PAROCO_E_SECRETARIA);
        MATRIZ.put(OperacaoSacramental.VINCULAR, PAROCO_E_SECRETARIA);
        MATRIZ.put(OperacaoSacramental.AVERBAR_DIGITAR, PAROCO_E_SECRETARIA);
        MATRIZ.put(OperacaoSacramental.EMITIR_CERTIDAO, PAROCO_E_SECRETARIA);
        MATRIZ.put(OperacaoSacramental.HOMOLOGAR, PAROCO);
        MATRIZ.put(OperacaoSacramental.HOMOLOGAR_AVERBACAO, PAROCO);
        MATRIZ.put(OperacaoSacramental.RETIFICAR, PAROCO);
        MATRIZ.put(OperacaoSacramental.CANCELAR, PAROCO);
    }

    /**
     * Decide se o papel pode executar a operação sobre o assento.
     *
     * @param papelArmazenado papel global resolvido do ator (código armazenado ou nome do enum)
     * @return {@code true} se o papel pode executar a operação sobre assento da visibilidade dada
     */
    public boolean permitida(String papelArmazenado, OperacaoSacramental operacao, Visibilidade visibilidade) {
        PapelGlobal papel = PapelGlobal.fromStoredValue(papelArmazenado);
        if (papel == null || !MATRIZ.get(operacao).contains(papel)) {
            return false;
        }
        return visibilidade != Visibilidade.RESERVADO || LIVRO_RESERVADO.contains(papel);
    }

    /**
     * Indica se o papel enxerga o livro reservado em consultas e listagens.
     *
     * @param papelArmazenado papel global resolvido do ator
     * @return {@code true} se o acesso ao livro reservado é permitido
     */
    public boolean acessaLivroReservado(String papelArmazenado) {
        PapelGlobal papel = PapelGlobal.fromStoredValue(papelArmazenado);
        return papel != null && LIVRO_RESERVADO.contains(papel);
    }
}
