package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;

/**
 * Snapshot imutável dos dados que as {@link RegraTipologiaOrganizacao} avaliam. O caso de uso carrega o
 * catálogo oficial e a organização-mãe e entrega aqui, mantendo as policies sem I/O.
 *
 * @param setorPastoral múnus informado para a organização, ou {@code null}
 * @param subtipoConselho natureza do conselho informada, ou {@code null}
 * @param oficial item do catálogo oficial referenciado, ou {@code null} quando não informado
 * @param organizacaoMae organização-mãe quando esta for outra organização; {@code null} se ausente ou territorial
 * @param possuiMae {@code true} quando algum pai foi informado (organização ou unidade territorial)
 */
public record ContextoTipologia(
        SetorPastoral setorPastoral,
        SubtipoConselho subtipoConselho,
        PastoralMovimentoOficial oficial,
        Organizacao organizacaoMae,
        boolean possuiMae) {
}
