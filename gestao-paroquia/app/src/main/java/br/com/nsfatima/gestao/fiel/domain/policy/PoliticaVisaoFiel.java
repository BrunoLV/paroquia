package br.com.nsfatima.gestao.fiel.domain.policy;

import java.util.Locale;
import java.util.Set;

/**
 * Minimização de dados na consulta de fiéis (LGPD, RN-LGP-02): cada papel enxerga apenas o necessário à sua
 * função. Quem cuida do cadastro (Admin, Pároco, Bispo, Secretaria) vê o registro completo e não é limitado a
 * uma organização; o coordenador vê nome e meios de contato dos membros do próprio grupo; os papéis
 * consultivos (conselho, vigário) e qualquer outro papel veem apenas identificador e nome.
 *
 * <p>Stateless e sem I/O; o papel desconhecido cai sempre na visão mais restrita.</p>
 */
public final class PoliticaVisaoFiel {

    /** Nível de detalhe da consulta de fiéis. */
    public enum Visao {
        /** Registro completo, sem restrição de organização. */
        COMPLETA,
        /** Nome e contato, restrito aos membros do grupo do coordenador. */
        CONTATO,
        /** Somente identificador e nome. */
        NOME
    }

    private static final Set<String> CADASTRO = Set.of("admin", "paroco", "bispo-diocesano", "secretaria-paroquial");

    private PoliticaVisaoFiel() {
    }

    /**
     * Determina a visão a que o papel tem direito.
     *
     * @param papel papel do ator, em qualquer caixa e separador
     * @return a visão correspondente; {@link Visao#NOME} para papel nulo ou desconhecido
     */
    public static Visao para(String papel) {
        if (papel == null) {
            return Visao.NOME;
        }
        String normalizado = papel.trim().toLowerCase(Locale.ROOT).replace("role_", "").replace('_', '-');
        if (CADASTRO.contains(normalizado)) {
            return Visao.COMPLETA;
        }
        return "coordenador".equals(normalizado) ? Visao.CONTATO : Visao.NOME;
    }
}
