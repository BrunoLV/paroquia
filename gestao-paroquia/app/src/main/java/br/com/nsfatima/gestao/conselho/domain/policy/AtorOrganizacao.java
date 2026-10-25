package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Set;

/**
 * Quem age sobre a ata, reduzido ao que a autorização precisa: os papéis globais do usuário e os papéis que
 * ele exerce na organização convocante da ata. É um retrato imutável montado pela camada de aplicação, para
 * que a {@link AutorizacaoAtaPolicy} não toque em segurança nem em persistência.
 */
public record AtorOrganizacao(Set<PapelGlobal> papeisGlobais, Set<PapelOrganizacional> papeisNaOrganizacao) {

    public AtorOrganizacao {
        papeisGlobais = Set.copyOf(papeisGlobais);
        papeisNaOrganizacao = Set.copyOf(papeisNaOrganizacao);
    }
}
