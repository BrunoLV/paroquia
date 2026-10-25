package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Fonte única de quais papéis um vínculo pode ter em cada tipo de organização. Quem cria ou altera vínculo
 * (cadastro de membros, promoção, mandato de coordenação) e quem lê o vínculo para decidir autorização (contexto
 * do calendário) consultam esta tabela, para que nunca discordem.
 *
 * <p>O coordenador e o vice-coordenador de qualquer organização, conselho inclusive, são membros dela: o vínculo
 * do fiel é promovido ao papel de coordenador ({@link PapelOrganizacional#COORDENADOR}) ou de vice, que usa o
 * papel de secretário de grupo ({@link br.com.nsfatima.gestao.organizacao.domain.model.FuncaoCoordenacao}).
 * Quando o mandato termina, o vínculo volta ao papel de membro comum do tipo ({@link #papelDeMembro}).</p>
 */
public final class PapeisPorTipoOrganizacao {

    private static final Set<PapelOrganizacional> DE_GRUPO = EnumSet.of(PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.SECRETARIO_GRUPO, PapelOrganizacional.MEMBRO_ATIVO, PapelOrganizacional.VOLUNTARIO);

    private static final Map<TipoOrganizacao, Set<PapelOrganizacional>> PAPEIS = new EnumMap<>(Map.of(
            TipoOrganizacao.PASTORAL, DE_GRUPO,
            TipoOrganizacao.MOVIMENTO, DE_GRUPO,
            TipoOrganizacao.SERVICO_PAROQUIAL, DE_GRUPO,
            TipoOrganizacao.CONSELHO, EnumSet.of(PapelOrganizacional.SECRETARIO_CONSELHO,
                    PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL, PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO,
                    PapelOrganizacional.COORDENADOR, PapelOrganizacional.SECRETARIO_GRUPO)));

    private PapeisPorTipoOrganizacao() {
    }

    /**
     * Papéis admitidos no tipo de organização.
     *
     * @param tipo tipo da organização
     * @return os papéis permitidos
     */
    public static Set<PapelOrganizacional> permitidos(TipoOrganizacao tipo) {
        return Set.copyOf(PAPEIS.get(tipo));
    }

    /**
     * Indica se o tipo de organização admite o papel.
     *
     * @param tipo tipo da organização
     * @param papel papel do vínculo
     * @return {@code true} se a combinação é válida
     */
    public static boolean aceita(TipoOrganizacao tipo, PapelOrganizacional papel) {
        return tipo != null && papel != null && PAPEIS.get(tipo).contains(papel);
    }

    /**
     * Exige que o tipo de organização admita o papel.
     *
     * @throws IllegalArgumentException se a combinação não é válida
     */
    public static void exigir(TipoOrganizacao tipo, PapelOrganizacional papel) {
        if (!aceita(tipo, papel)) {
            throw new IllegalArgumentException("O papel " + (papel == null ? "informado" : papel.getDescricao())
                    + " não existe em organização do tipo " + tipo + ".");
        }
    }

    /**
     * Papel de membro comum do tipo, para onde volta quem deixa a coordenação.
     *
     * @param tipo tipo da organização
     * @param subtipo subtipo do conselho; ignorado nos demais tipos
     * @return membro do conselho pastoral ou econômico, ou membro ativo
     */
    public static PapelOrganizacional papelDeMembro(TipoOrganizacao tipo, SubtipoConselho subtipo) {
        if (tipo != TipoOrganizacao.CONSELHO) {
            return PapelOrganizacional.MEMBRO_ATIVO;
        }
        return subtipo == SubtipoConselho.CAEP
                ? PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO : PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL;
    }
}
