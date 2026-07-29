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
            case PASTORAL -> normalizedRole == PapelOrganizacional.COORDENADOR_PASTORAL_GLOBAL
                    || normalizedRole == PapelOrganizacional.COORDENADOR_PASTORAL_LOCAL
                    || normalizedRole == PapelOrganizacional.COORDENADOR_PASTORAL
                    || normalizedRole == PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                    || normalizedRole == PapelOrganizacional.MEMBRO_PASTORAL;
            case MOVIMENTO -> normalizedRole == PapelOrganizacional.COORDENADOR_MOVIMENTO
                    || normalizedRole == PapelOrganizacional.MEMBRO_MOVIMENTO;
            case MATRIZ, COMUNIDADE -> normalizedRole == PapelOrganizacional.COORDENADOR_PASTORAL_LOCAL
                    || normalizedRole == PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL
                    || normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                    || normalizedRole == PapelOrganizacional.MEMBRO_CPC;
            case CLERO -> normalizedRole == PapelOrganizacional.PAROCO
                    || normalizedRole == PapelOrganizacional.VIGARIO
                    || normalizedRole == PapelOrganizacional.PADRE
                    || normalizedRole == PapelOrganizacional.DIACONO;
            case CPP, CPC, CONSELHO_PASTORAL, CONSELHO_ECONOMICO -> normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                    || normalizedRole == PapelOrganizacional.MEMBRO_CPC
                    || normalizedRole == PapelOrganizacional.MEMBRO_CPP
                    || normalizedRole == PapelOrganizacional.MESA_DIRETORA_CPP
                    || normalizedRole == PapelOrganizacional.MEMBRO_CAE
                    || normalizedRole == PapelOrganizacional.SECRETARIO;
            case OUTRO -> false;
        };
    }
}
