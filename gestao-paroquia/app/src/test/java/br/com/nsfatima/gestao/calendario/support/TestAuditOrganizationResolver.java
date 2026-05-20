package br.com.nsfatima.gestao.calendario.support;

import java.util.Map;
import java.util.UUID;

public final class TestAuditOrganizationResolver {

    private static final Map<String, String> USER_ORG_MAP = Map.of(
            "joao.silva", "00000000-0000-0000-0000-0000000000aa",
            "maria.secretaria", "00000000-0000-0000-0000-0000000000cc",
            "pedro.membro", "00000000-0000-0000-0000-0000000000aa",
            "ana.conselho", "00000000-0000-0000-0000-0000000000cc",
            "clara.invalida", "00000000-0000-0000-0000-0000000000dd"
    );

    public static String resolveOrgId(String username) {
        return USER_ORG_MAP.get(username);
    }
}
