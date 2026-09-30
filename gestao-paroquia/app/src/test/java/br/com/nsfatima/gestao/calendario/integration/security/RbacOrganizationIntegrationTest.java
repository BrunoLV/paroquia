package br.com.nsfatima.gestao.calendario.integration.security;

import br.com.nsfatima.gestao.calendario.domain.policy.CleroCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.CompatibilidadePapelOrganizacionalPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ConselhoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.MovimentoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PastoralCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RbacOrganizationIntegrationTest {

    private final List<CompatibilidadePapelOrganizacionalPolicy> policies = List.of(
            new CleroCompatibilidadePapelPolicy(), new ConselhoCompatibilidadePapelPolicy(),
            new MovimentoCompatibilidadePapelPolicy(), new PastoralCompatibilidadePapelPolicy());

    @Test
    void deveImporPapelCatalogPorGrupoParoquialTipo() {
        assertTrue(aceita("CONSELHO", PapelOrganizacional.SECRETARIO_CONSELHO));
        assertFalse(aceita("CLERO", PapelOrganizacional.SECRETARIO_CONSELHO));
        assertTrue(aceita("PASTORAL", PapelOrganizacional.COORDENADOR));
    }

    private boolean aceita(String tipo, PapelOrganizacional papel) {
        return policies.stream().anyMatch(policy -> policy.aceita(tipo, papel));
    }
}
