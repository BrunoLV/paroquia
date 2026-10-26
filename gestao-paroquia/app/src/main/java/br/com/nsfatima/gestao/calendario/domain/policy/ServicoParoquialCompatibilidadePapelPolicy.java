package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;

/** Papéis de vínculo válidos num serviço paroquial, conforme a tabela única de papéis por tipo. */
public final class ServicoParoquialCompatibilidadePapelPolicy implements CompatibilidadePapelOrganizacionalPolicy {

    @Override
    public boolean aceita(String tipoOrganizacao, PapelOrganizacional papel) {
        return "SERVICO_PAROQUIAL".equalsIgnoreCase(tipoOrganizacao)
                && PapeisPorTipoOrganizacao.aceita(TipoOrganizacao.SERVICO_PAROQUIAL, papel);
    }
}
