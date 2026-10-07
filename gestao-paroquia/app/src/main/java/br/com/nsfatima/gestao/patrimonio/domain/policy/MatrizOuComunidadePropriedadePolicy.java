package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoOrganizacaoInvalidaException;

/**
 * Mantém a separação patrimonial paroquial: somente matriz e comunidade podem possuir ativos.
 */
public final class MatrizOuComunidadePropriedadePolicy implements PropriedadeUnidadeTerritorialPolicy {

    @Override
    public void assertPermitida(TipoUnidadeTerritorial tipo) {
        if (tipo != TipoUnidadeTerritorial.MATRIZ && tipo != TipoUnidadeTerritorial.COMUNIDADE) {
            throw new AtivoOrganizacaoInvalidaException(
                    "Ativos patrimoniais só podem pertencer a unidades territoriais do tipo MATRIZ ou COMUNIDADE");
        }
    }
}
