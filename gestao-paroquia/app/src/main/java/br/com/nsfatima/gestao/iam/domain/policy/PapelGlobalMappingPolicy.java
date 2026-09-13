package br.com.nsfatima.gestao.iam.domain.policy;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;

/**
 * Contrato puro de política para derivar papéis globais (acesso irrestrito) a partir de
 * funções eclesiásticas e de strings de autoridade legadas.
 */
public interface PapelGlobalMappingPolicy {

    /**
     * Mapeia uma função eclesiástica institucional para o papel global equivalente.
     *
     * @param funcao função eclesiástica do encargo, ou {@code null}
     * @return papel global correspondente, ou {@code null} se a função não conferir acesso global
     */
    PapelGlobal mapFuncaoToPapelGlobal(FuncaoEclesiastica funcao);

    /**
     * Verifica se a string de papéis (legado, separada por vírgula) contém algum papel global.
     *
     * @param roles papéis do usuário em formato legado, ou {@code null}
     * @return {@code true} se algum papel na string conferir acesso global
     */
    boolean hasGlobalRole(String roles);
}
