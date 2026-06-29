package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationPolicy {

    public boolean isRoleAllowed(String organizationType, String role) {
        TipoOrganizacao normalizedOrg = TipoOrganizacao.fromStoredValue(organizationType);
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(role);

        if (normalizedOrg == null || normalizedRole == null) {
            return false;
        }

        return switch (normalizedOrg) {
            case PASTORAL, MOVIMENTO, LAICATO, MATRIZ, COMUNIDADE -> normalizedRole == PapelOrganizacional.COORDENADOR
                    || normalizedRole == PapelOrganizacional.VICE_COORDENADOR
                    || normalizedRole == PapelOrganizacional.MEMBRO;
            case CLERO -> normalizedRole == PapelOrganizacional.PAROCO
                    || normalizedRole == PapelOrganizacional.VIGARIO
                    || normalizedRole == PapelOrganizacional.PADRE;
            case CONSELHO, CONSELHO_ECONOMICO -> normalizedRole == PapelOrganizacional.COORDENADOR
                    || normalizedRole == PapelOrganizacional.VICE_COORDENADOR
                    || normalizedRole == PapelOrganizacional.SECRETARIO
                    || normalizedRole == PapelOrganizacional.MEMBRO;
            case OUTRO -> false;
        };
    }
}
