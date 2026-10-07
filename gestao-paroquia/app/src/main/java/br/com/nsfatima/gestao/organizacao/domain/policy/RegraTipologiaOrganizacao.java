package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import java.util.List;

/**
 * Policy que concentra as invariantes de um {@link TipoOrganizacao}. Uma implementação por tipo evita cadeias
 * de {@code if/switch} no serviço; é stateless e não faz I/O.
 */
public interface RegraTipologiaOrganizacao {

    /**
     * Tipo de organização a que esta regra se aplica.
     *
     * @return o tipo da regra
     */
    TipoOrganizacao tipo();

    /**
     * Avalia o contexto contra as invariantes do tipo.
     *
     * @param contexto dados já carregados pelo caso de uso
     * @return mensagens de violação; lista vazia quando o contexto é válido
     */
    List<String> violacoes(ContextoTipologia contexto);
}
