package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.calendario.domain.policy.CleroCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.CompatibilidadePapelOrganizacionalPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ConselhoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.MovimentoCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.PastoralCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ServicoParoquialCompatibilidadePapelPolicy;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleCatalogContractTest {

    private final List<CompatibilidadePapelOrganizacionalPolicy> policies = List.of(
            new CleroCompatibilidadePapelPolicy(), new ConselhoCompatibilidadePapelPolicy(),
            new MovimentoCompatibilidadePapelPolicy(), new PastoralCompatibilidadePapelPolicy(),
            new ServicoParoquialCompatibilidadePapelPolicy());

    @Test
    void deveRejeitarSecretarioForaConselho() {
        assertFalse(aceita("PASTORAL", PapelOrganizacional.SECRETARIO_CONSELHO));
    }

    @Test
    void asPoliticasDoCalendarioConcordamComATabelaUnicaDePapeisPorTipo() {
        for (TipoOrganizacao tipo : TipoOrganizacao.values()) {
            for (PapelOrganizacional papel : PapelOrganizacional.values()) {
                assertEquals(PapeisPorTipoOrganizacao.aceita(tipo, papel), aceita(tipo.name(), papel),
                        papel + " em " + tipo);
            }
        }
    }

    private boolean aceita(String tipo, PapelOrganizacional papel) {
        return policies.stream().anyMatch(policy -> policy.aceita(tipo, papel));
    }
}
