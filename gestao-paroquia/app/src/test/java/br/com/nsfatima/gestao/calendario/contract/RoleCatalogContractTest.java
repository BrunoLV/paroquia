package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.calendario.domain.policy.CleroCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.CompatibilidadePapelOrganizacionalPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ConselhoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.MovimentoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PastoralCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RoleCatalogContractTest {

    private final List<CompatibilidadePapelOrganizacionalPolicy> policies = List.of(
            new CleroCompatibilidadePapelPolicy(), new ConselhoCompatibilidadePapelPolicy(),
            new MovimentoCompatibilidadePapelPolicy(), new PastoralCompatibilidadePapelPolicy());

    @Test
    void deveRejeitarSecretarioForaConselho() {
        assertFalse(aceita("PASTORAL", PapelOrganizacional.SECRETARIO_CONSELHO));
    }

    private boolean aceita(String tipo, PapelOrganizacional papel) {
        return policies.stream().anyMatch(policy -> policy.aceita(tipo, papel));
    }
}
