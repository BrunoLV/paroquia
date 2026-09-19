package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Implementação pura da política de autorização de patrimônio.
 */
public final class PadraoAutorizacaoPatrimonioPolicy implements AutorizacaoPatrimonioPolicy {

    private static final Set<PapelOrganizacional> PATRIMONIO_EDITOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.COORD_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP);

    @Override
    public boolean isAtorDeAltoNivel(PapelOrganizacional papel) {
        if (papel == null) {
            return false;
        }
        return papel == PapelOrganizacional.ADMIN
                || papel == PapelOrganizacional.PAROCO
                || papel == PapelOrganizacional.PRESIDENTE_CPC
                || papel == PapelOrganizacional.MESA_CPP
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL;
    }

    @Override
    public void assertPodeGerirPatrimonio(PapelOrganizacional papel, UUID atorOrganizacaoId, UUID grupoParoquialId) {
        if (!PATRIMONIO_EDITOR_ROLES.contains(papel)) {
            throw new OperacaoProibidaException("Usuário não tem permissão para gerir ativos patrimoniais");
        }

        if (!isAtorDeAltoNivel(papel)
                && atorOrganizacaoId != null
                && !Objects.equals(atorOrganizacaoId, grupoParoquialId)) {
            throw new OperacaoProibidaException("Usuário não pode gerir ativos de outra unidade territorial");
        }
    }
}
