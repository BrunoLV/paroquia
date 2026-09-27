package br.com.nsfatima.gestao.iam.domain.policy;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;

import java.util.Arrays;
import java.util.Locale;

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
            case DIACONO_AUXILIAR -> PapelGlobal.DIACONO_PAROQUIAL;
            case EMERITO -> PapelGlobal.FIEL;
        };
    }

    @Override
    public boolean hasGlobalRole(String roles) {
        if (roles == null || roles.isBlank()) {
            return false;
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(r -> {
                    String norm = r.toUpperCase(Locale.ROOT)
                            .replace("ROLE_", "")
                            .replace("GLOBAL_", "")
                            .replace('-', '_');
                    return norm.equals("ADMIN")
                            || norm.equals("BISPO_DIOCESANO")
                            || norm.equals("PAROCO")
                            || norm.equals("VIGARIO")
                            || norm.equals("DIACONO_PAROQUIAL")
                            || norm.equals("SECRETARIA_PAROQUIAL")
                            || norm.equals("FIEL");
                });
    }
}
