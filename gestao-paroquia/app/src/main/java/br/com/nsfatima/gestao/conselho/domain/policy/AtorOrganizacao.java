package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Set;

/**
 * Quem age sobre a ata, reduzido ao que a autorização precisa: os papéis globais do usuário, os papéis que
 * ele exerce na organização convocante e, na reunião conjunta, os que exerce na organização associada (que
 * só dão leitura). É um retrato imutável montado pela camada de aplicação, para que a
 * {@link AutorizacaoAtaPolicy} não toque em segurança nem em persistência.
 */
public record AtorOrganizacao(
        Set<PapelGlobal> papeisGlobais,
        Set<PapelOrganizacional> papeisNaOrganizacao,
        Set<PapelOrganizacional> papeisNaAssociada) {

    public AtorOrganizacao {
        papeisGlobais = Set.copyOf(papeisGlobais);
        papeisNaOrganizacao = Set.copyOf(papeisNaOrganizacao);
        papeisNaAssociada = Set.copyOf(papeisNaAssociada);
    }

    /** Ator sem vínculo com organização associada. */
    public AtorOrganizacao(Set<PapelGlobal> papeisGlobais, Set<PapelOrganizacional> papeisNaOrganizacao) {
        this(papeisGlobais, papeisNaOrganizacao, Set.of());
    }
}
