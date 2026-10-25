package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Quem pode fazer o quê com uma ata. Stateless e zero-I/O: recebe o retrato do ator, as regras do tipo e o
 * status, e devolve o conjunto de {@link AcaoAta} permitidas, que também alimenta a interface.
 *
 * <ul>
 *   <li>Lê: clero e secretaria paroquial em qualquer órgão; os demais apenas se tiverem papel no órgão
 *   convocante. {@code ADMIN} não lê conteúdo de ata (mínimo privilégio).</li>
 *   <li>Redige, submete, reabre e retifica: somente os papéis redatores do tipo, no órgão convocante.</li>
 *   <li>Homologa e veta: privativo do Pároco (ou Bispo diocesano), nunca do redator (RN-CON-02).</li>
 *   <li>Vê a trilha de auditoria: Pároco, Bispo diocesano e secretaria paroquial.</li>
 * </ul>
 */
public final class AutorizacaoAtaPolicy {

    private static final Set<PapelGlobal> LEITURA_GLOBAL = Set.of(PapelGlobal.PAROCO, PapelGlobal.BISPO_DIOCESANO,
            PapelGlobal.VIGARIO, PapelGlobal.SECRETARIA_PAROQUIAL);
    private static final Set<PapelGlobal> HOMOLOGADORES = Set.of(PapelGlobal.PAROCO, PapelGlobal.BISPO_DIOCESANO);
    private static final Set<PapelGlobal> AUDITORES = Set.of(PapelGlobal.PAROCO, PapelGlobal.BISPO_DIOCESANO,
            PapelGlobal.SECRETARIA_PAROQUIAL);

    private AutorizacaoAtaPolicy() {
    }

    /**
     * Indica se os papéis globais dão leitura a atas de qualquer órgão, sem exigir vínculo com ele.
     *
     * @param papeisGlobais papéis globais do usuário
     * @return {@code true} para clero e secretaria paroquial
     */
    public static boolean leituraEmQualquerOrganizacao(Set<PapelGlobal> papeisGlobais) {
        return !Collections.disjoint(papeisGlobais, LEITURA_GLOBAL);
    }

    /**
     * Ações permitidas ao ator sobre uma ata.
     *
     * @param ator papéis globais e na organização convocante
     * @param regras regras do tipo de reunião da ata
     * @param status situação atual da ata
     * @return as ações permitidas, vazio se o ator não pode nem ler
     */
    public static Set<AcaoAta> acoesPermitidas(AtorOrganizacao ator, RegrasTipoReuniao regras, StatusAta status) {
        boolean leitor = leituraEmQualquerOrganizacao(ator.papeisGlobais()) || !ator.papeisNaOrganizacao().isEmpty();
        if (!leitor) {
            return Set.of();
        }
        Set<AcaoAta> acoes = EnumSet.of(AcaoAta.LER, AcaoAta.EXPORTAR);
        if (!Collections.disjoint(ator.papeisGlobais(), AUDITORES)) {
            acoes.add(AcaoAta.VER_TRILHA);
        }
        boolean redator = !Collections.disjoint(ator.papeisNaOrganizacao(), regras.papeisRedatores());
        boolean homologador = !Collections.disjoint(ator.papeisGlobais(), HOMOLOGADORES);
        acoes.addAll(acoesDoStatus(status, redator, homologador));
        return Collections.unmodifiableSet(acoes);
    }

    private static Set<AcaoAta> acoesDoStatus(StatusAta status, boolean redator, boolean homologador) {
        return switch (status) {
            case RASCUNHO -> redator ? EnumSet.of(AcaoAta.REDIGIR, AcaoAta.SUBMETER) : EnumSet.noneOf(AcaoAta.class);
            case PENDENTE_HOMOLOGACAO ->
                    homologador ? EnumSet.of(AcaoAta.HOMOLOGAR, AcaoAta.VETAR) : EnumSet.noneOf(AcaoAta.class);
            case VETADA -> redator ? EnumSet.of(AcaoAta.REABRIR) : EnumSet.noneOf(AcaoAta.class);
            case HOMOLOGADA, REGISTRADA -> redator ? EnumSet.of(AcaoAta.RETIFICAR) : EnumSet.noneOf(AcaoAta.class);
        };
    }
}
