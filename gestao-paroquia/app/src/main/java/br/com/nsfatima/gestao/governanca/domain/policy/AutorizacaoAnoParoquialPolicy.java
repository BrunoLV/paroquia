package br.com.nsfatima.gestao.governanca.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

/** Define quem pode alterar a governança anual da paróquia sem conhecer a segurança da infraestrutura. */
@FunctionalInterface
public interface AutorizacaoAnoParoquialPolicy {

    void assertPodeGerir(PapelOrganizacional papel, TipoGrupo tipoGrupo);
}
