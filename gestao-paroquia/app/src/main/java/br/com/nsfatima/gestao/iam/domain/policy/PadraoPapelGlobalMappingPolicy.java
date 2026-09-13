package br.com.nsfatima.gestao.iam.domain.policy;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;

import java.util.Arrays;

/**
 * Implementação pura da política de mapeamento de papéis globais, sem frameworks ou I/O.
 */
public final class PadraoPapelGlobalMappingPolicy implements PapelGlobalMappingPolicy {

    @Override
    public PapelGlobal mapFuncaoToPapelGlobal(FuncaoEclesiastica funcao) {
        if (funcao == null) {
            return null;
        }
        return switch (funcao) {
            case BISPO_DIOCESANO -> PapelGlobal.BISPO_DIOCESANO;
            case PAROCO, ADMINISTRADOR_PAROQUIAL -> PapelGlobal.PAROCO;
            case VIGARIO_PAROQUIAL -> PapelGlobal.VIGARIO;
            default -> null;
        };
    }

    @Override
    public boolean hasGlobalRole(String roles) {
        if (roles == null || roles.isBlank()) {
            return false;
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(r -> r.equalsIgnoreCase("ADMIN")
                        || r.equalsIgnoreCase("PAROCO")
                        || r.equalsIgnoreCase("VIGARIO")
                        || r.equalsIgnoreCase("BISPO_DIOCESANO")
                        || r.equalsIgnoreCase("ROLE_ADMIN")
                        || r.equalsIgnoreCase("ROLE_PAROCO")
                        || r.equalsIgnoreCase("ROLE_VIGARIO")
                        || r.equalsIgnoreCase("ROLE_BISPO_DIOCESANO"));
    }
}
