package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

/**
 * Define uma Strategy de compatibilidade entre um escopo organizacional e um papel eclesial.
 *
 * DECISÃO DE DESIGN (OCP): cada escopo possui sua própria regra, impedindo que a expansão do
 * catálogo de vínculos reintroduza uma seleção condicional centralizada.
 */
@FunctionalInterface
public interface CompatibilidadePapelOrganizacionalPolicy {

    /**
     * Informa se o papel é canonicamente aceito pelo escopo recebido.
     *
     * @param tipoOrganizacao código de escopo já normalizado pela fronteira de segurança
     * @param papel papel organizacional já convertido para o vocabulário de domínio
     * @return {@code true} quando esta Strategy reconhece e permite a combinação
     */
    boolean aceita(String tipoOrganizacao, PapelOrganizacional papel);
}
