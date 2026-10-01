package br.com.nsfatima.gestao.sacramento.domain.policy;

import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;

import java.util.List;

/**
 * Estratégia dos requisitos canônicos de homologação de um sacramento. Stateless e zero-I/O: recebe
 * o teor em memória e devolve as pendências, sem alterar o agregado.
 */
public interface RequisitosAssentoPolicy {

    TipoSacramento tipo();

    /**
     * Verifica o teor contra os requisitos canônicos do sacramento.
     *
     * @param teor conteúdo documental do assento
     * @return pendências que impedem a homologação; vazia se o teor é válido
     */
    List<String> pendencias(TeorAssento teor);
}
