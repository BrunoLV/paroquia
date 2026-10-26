package br.com.nsfatima.gestao.clero.domain.policy;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;

import java.util.Locale;
import java.util.Set;

/**
 * Provisão de encargo clerical (nomear Pároco, Vigário, Diácono...) é ato de governo eclesial: cabe apenas ao
 * Pároco e ao Bispo Diocesano. Quem apenas cadastra clérigos (secretaria) e o Admin técnico, que não assina atos
 * de governo, podem manter dados cadastrais, mas jamais conferir ou alterar a função de um clérigo — a função
 * se converte em papel global de acesso irrestrito, então conferi-la equivale a conceder privilégio.
 *
 * <p>Stateless e sem I/O.</p>
 */
public final class PoliticaProvisaoEncargo {

    private static final Set<String> PAPEIS_COM_ALCADA = Set.of("paroco", "bispo-diocesano");

    /** Funções que se convertem no papel global de Pároco e, por isso, não admitem mais de um titular ativo. */
    private static final Set<FuncaoEclesiastica> FUNCOES_DE_PAROCO =
            Set.of(FuncaoEclesiastica.PAROCO, FuncaoEclesiastica.ADMINISTRADOR_PAROQUIAL);

    private PoliticaProvisaoEncargo() {
    }

    /**
     * Indica se o papel do ator tem alçada para conferir ou alterar a função de um clérigo.
     *
     * @param papelAtor papel do ator (com ou sem prefixo {@code ROLE_}, em qualquer caixa e separador)
     * @return {@code true} apenas para Pároco e Bispo Diocesano
     */
    public static boolean temAlcada(String papelAtor) {
        if (papelAtor == null) {
            return false;
        }
        String normalizado = papelAtor.trim().toLowerCase(Locale.ROOT).replace("role_", "").replace('_', '-');
        return PAPEIS_COM_ALCADA.contains(normalizado);
    }

    /**
     * Indica se a função se converte em Pároco e, portanto, tem titular único ativo na paróquia.
     *
     * @param funcao função a conferir
     * @return {@code true} para Pároco e Administrador Paroquial
     */
    public static boolean exigeTitularUnico(FuncaoEclesiastica funcao) {
        return FUNCOES_DE_PAROCO.contains(funcao);
    }

    /**
     * Funções que ocupam o lugar de Pároco, para a checagem de titular único.
     *
     * @return conjunto imutável de funções
     */
    public static Set<FuncaoEclesiastica> funcoesDeParoco() {
        return FUNCOES_DE_PAROCO;
    }
}
