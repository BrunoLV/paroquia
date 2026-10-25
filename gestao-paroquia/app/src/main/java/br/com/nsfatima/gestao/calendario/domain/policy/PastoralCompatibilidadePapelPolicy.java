package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;

/** Papéis de vínculo válidos numa pastoral, conforme a tabela única de papéis por tipo. */
public final class PastoralCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "PASTORAL".equalsIgnoreCase(tipoOrganizacao)
                && PapeisPorTipoOrganizacao.aceita(TipoOrganizacao.PASTORAL, papel);
    }
}
