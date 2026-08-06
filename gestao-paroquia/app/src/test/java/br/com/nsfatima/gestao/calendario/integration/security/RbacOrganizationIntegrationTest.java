package br.com.nsfatima.gestao.calendario.integration.security;

import br.com.nsfatima.gestao.calendario.domain.policy.AuthorizationPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacOrganizationIntegrationTest {

    private final AuthorizationPolicy policy = new AuthorizationPolicy();

    @Test
    void deveImporPapelCatalogPorGrupoParoquialTipo() {
        assertTrue(policy.isRoleAllowed("CONSELHO_PASTORAL", "secretario"));
        assertFalse(policy.isRoleAllowed("CLERO", "secretario"));
        assertTrue(policy.isRoleAllowed("PASTORAL", "coordenador-pastoral-local"));
    }
}
