package br.com.nsfatima.gestao.sdd;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.application.service.EscopoUsuarioAuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class SddCenario3AcessoVigarioIntegrationTest {

    @Autowired
    private EscopoUsuarioAuthorizationService escopoUsuarioAuthorizationService;

    @Test
    @DisplayName("Cenário 3 BDD: Vigário possui acesso total a Conselho sem vínculo como membro")
    void devePermitirAcessoIrrestritoAoVigarioSemVinculoPrevio() {
        // A autorização do clero é independente de cadastro de fiel e de vínculo pastoral.
        UUID conselhoId = UUID.randomUUID();
        UUID matrizId = UUID.randomUUID();

        boolean canManageConselho = escopoUsuarioAuthorizationService.canManageOrganizationData(
                "vigario",
                TipoGrupo.CONSELHO,
                conselhoId,
                matrizId,
                matrizId
        );

        boolean canApproveEventsConselho = escopoUsuarioAuthorizationService.canApproveEvents(
                "vigario",
                TipoGrupo.CONSELHO,
                conselhoId,
                matrizId
        );

        boolean canApproveEventsParoco = escopoUsuarioAuthorizationService.canApproveEvents(
                "paroco",
                TipoGrupo.CONSELHO,
                conselhoId,
                matrizId
        );

        assertThat(canManageConselho).isTrue();
        assertThat(canApproveEventsConselho).isFalse();
        assertThat(canApproveEventsParoco).isTrue();
    }
}
