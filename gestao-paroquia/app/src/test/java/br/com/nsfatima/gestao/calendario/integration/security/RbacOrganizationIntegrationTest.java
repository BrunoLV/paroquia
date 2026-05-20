package br.com.nsfatima.gestao.calendario.integration.security;

import br.com.nsfatima.gestao.calendario.domain.policy.AuthorizationPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacOrganizationIntegrationTest {

    private final AuthorizationPolicy policy = new AuthorizationPolicy();

    @Test
    void shouldEnforceRoleCatalogByOrganizationType() {
        assertTrue(policy.isRoleAllowed("CONSELHO", "secretario"));
        assertFalse(policy.isRoleAllowed("CLERO", "secretario"));
        assertTrue(policy.isRoleAllowed("PASTORAL", "coordenador"));
    }
}
