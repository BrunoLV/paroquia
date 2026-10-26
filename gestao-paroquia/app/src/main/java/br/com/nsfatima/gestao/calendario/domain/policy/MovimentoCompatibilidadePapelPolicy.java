package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;

/** Papéis de vínculo válidos num movimento, conforme a tabela única de papéis por tipo. */
public final class MovimentoCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "MOVIMENTO".equalsIgnoreCase(tipoOrganizacao)
                && PapeisPorTipoOrganizacao.aceita(TipoOrganizacao.MOVIMENTO, papel);
    }
}
