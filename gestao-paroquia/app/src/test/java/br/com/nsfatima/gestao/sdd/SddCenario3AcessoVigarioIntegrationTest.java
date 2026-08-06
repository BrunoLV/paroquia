package br.com.nsfatima.gestao.sdd;

import br.com.nsfatima.gestao.fiel.api.v1.dto.CreateFielRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.CreateFielUseCase;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.iam.application.usecase.AtribuirPapelGlobalUseCase;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.domain.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.service.EscopoUsuarioAuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "ADMIN")
class SddCenario3AcessoVigarioIntegrationTest {

    @Autowired
    private CreateFielUseCase createFielUseCase;

    @Autowired
    private AtribuirPapelGlobalUseCase atribuirPapelGlobalUseCase;

    @Autowired
    private PapelGlobalAuthorizationService papelGlobalAuthorizationService;

    @Autowired
    private EscopoUsuarioAuthorizationService escopoUsuarioAuthorizationService;

    @Test
    @DisplayName("Cenário 3 BDD: Usuário/Fiel com papel VIGARIO em papel_global possui acesso total a Conselho sem vínculo prévio como membro")
    void devePermitirAcessoIrrestritoAoVigarioSemVinculoPrevio() {
        // 1. Cadastra Fiel do tipo Sacerdote
        CreateFielRequest reqVigario = new CreateFielRequest(
                "Padre Vigário " + UUID.randomUUID(),
                "333.444.555-66",
                LocalDate.of(1975, 8, 10),
                "vigario@paroquia.org",
                "(11) 95555-4444",
                "Casa Paroquial, 10",
                Vocacao.SACERDOTE,
                true, true, true, false
        );

        FielResponse fielVigario = createFielUseCase.execute(reqVigario, "test-actor");

        // 2. Atribui papel global VIGARIO
        atribuirPapelGlobalUseCase.execute(fielVigario.id(), "VIGARIO");

        // 3. Consulta papéis globais do Fiel
        Set<PapelGlobal> papeis = papelGlobalAuthorizationService.papeisGlobaisDe(fielVigario.id());
        assertThat(papeis).contains(PapelGlobal.VIGARIO);

        // 4. Valida permissão de gestão de dados de organização/conselho para VIGARIO sem precisar de vínculo em MembroGrupo
        UUID conselhoId = UUID.randomUUID();
        UUID matrizId = UUID.randomUUID();

        boolean canManageConselho = escopoUsuarioAuthorizationService.canManageOrganizationData(
                PapelOrganizacional.VIGARIO,
                TipoGrupo.CONSELHO,
                conselhoId,
                matrizId,
                matrizId
        );

        boolean canApproveEventsConselho = escopoUsuarioAuthorizationService.canApproveEvents(
                PapelOrganizacional.VIGARIO,
                TipoGrupo.CONSELHO,
                conselhoId,
                matrizId
        );

        assertThat(canManageConselho).isTrue();
        assertThat(canApproveEventsConselho).isTrue();
    }
}
