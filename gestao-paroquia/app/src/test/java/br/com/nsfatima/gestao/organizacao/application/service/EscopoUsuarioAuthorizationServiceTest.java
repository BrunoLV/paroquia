package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.policy.PadraoAutorizacaoEscopoUsuarioPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserScopeAuthorizationServiceTest {

    private final UUID matrizId = UUID.randomUUID();
    private final UUID comunidadeBId = UUID.randomUUID();
    private final UUID pastoralMatrizId = UUID.randomUUID();
    private final UUID pastoralComunidadeBId = UUID.randomUUID();
    private EscopoUsuarioAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new EscopoUsuarioAuthorizationService(new PadraoAutorizacaoEscopoUsuarioPolicy());
    }

    @Nested
    @DisplayName("ADMIN, PAROCO and VIGARIO Permissions")
    class AdminAndCleroGeneralPermissions {

        @Test
        @DisplayName("ADMIN has full global access for any action and target scope")
        void adminHasFullAccess() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.ADMIN,
                    TipoGrupo.PASTORAL, matrizId, matrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.ADMIN,
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.ADMIN,
                    TipoGrupo.PASTORAL, matrizId, comunidadeBId)).isTrue();

        }

        @Test
        @DisplayName("PAROCO and VIGARIO have full general management access")
        void parocoAndVigarioHaveFullGeneralAccess() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.PAROCO,
                    TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.VIGARIO,
                    TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.PAROCO,
                    TipoGrupo.CONSELHO, null, matrizId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.VIGARIO,
                    TipoGrupo.CONSELHO, null, comunidadeBId)).isTrue();

        }
    }

    @Nested
    @DisplayName("Conselho Pastoral Leadership Scope Permissions")
    class ConselhoPastoralScopePermissions {

        @Test
        @DisplayName("Conselho Pastoral Coordinator can manage data within their own community")
        void conselhoCoordinatorCommunityScope() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoGrupo.CONSELHO, matrizId, matrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.MESA_CPP,
                    TipoGrupo.CONSELHO, matrizId, pastoralMatrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId, comunidadeBId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoGrupo.CONSELHO, matrizId, pastoralComunidadeBId, comunidadeBId)).isFalse();

        }

        @Test
        @DisplayName("Conselho Pastoral Coordinator can approve events in their community")
        void conselhoCoordinatorCanApproveEventsInCommunity() {
            assertThat(service.canApproveEvents(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoGrupo.CONSELHO, matrizId, matrizId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId)).isFalse();

        }
    }

    @Nested
    @DisplayName("Pastoral/Movimento Leadership Scope Permissions")
    class PastoralLeadershipScopePermissions {

        @Test
        @DisplayName("Pastoral Coordinator can only manage data for their exact Pastoral/Movimento")
        void pastoralCoordinatorExactPastoralScope() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_MOVIMENTO,
                    TipoGrupo.MOVIMENTO, pastoralMatrizId, pastoralMatrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralComunidadeBId, comunidadeBId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, matrizId, matrizId)).isFalse();

        }

        @Test
        @DisplayName("Pastoral Coordinator cannot directly approve events (requires Conselho/Paroco aprovacao)")
        void pastoralCoordinatorCannotApproveEvents() {
            assertThat(service.canApproveEvents(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId)).isFalse();

        }
    }

    @Nested
    @DisplayName("PADRE, SECRETARIO and VOLUNTARIO Scope Permissions")
    class ReadOnlyAndMemberPermissions {

        @Test
        @DisplayName("PADRE and SECRETARIO cannot perform general mutations, but can view and add observations")
        void padreAndSecretarioPermissions() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.PADRE,
                    TipoGrupo.CONSELHO, null, matrizId, matrizId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.SECRETARIO,
                    TipoGrupo.CONSELHO, matrizId, matrizId, matrizId)).isFalse();

            assertThat(service.canAddObservation(PapelOrganizacional.PADRE,
                    TipoGrupo.CONSELHO, null, matrizId, false)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.SECRETARIO,
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId, false)).isTrue();

        }

        @Test
        @DisplayName("VOLUNTARIO can only add observations if event belongs to their pastoral or pastoral is participant")
        void membroAddObservationPermissions() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId, matrizId)).isFalse();

            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId, false)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, comunidadeBId, true)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralComunidadeBId, false)).isFalse();

        }

        @Test
        @DisplayName("VOLUNTARIO cannot view unapproved/pendente events")
        void membroCannotViewPendenteEvents() {
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL)).isFalse();
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.PADRE)).isTrue();
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.SECRETARIO)).isTrue();
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.COORD_PASTORAL_LOCAL)).isTrue();
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.PAROCO)).isTrue();
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.ADMIN)).isTrue();
        }
    }

    @Nested
    @DisplayName("Territorial Scope Permissions")
    class TerritorialScopePermissions {

        @Test
        @DisplayName("Global roles have cross-community access")
        void globalRolesHaveCrossCommunityAccess() {
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.ADMIN, matrizId, comunidadeBId)).isTrue();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.PAROCO, matrizId, comunidadeBId)).isTrue();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.COORD_PASTORAL_GLOBAL, matrizId,
                    comunidadeBId)).isTrue();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.COORD_MOVIMENTO, matrizId,
                    comunidadeBId)).isTrue();
        }

        @Test
        @DisplayName("Local roles are restricted strictly to their community")
        void localRolesRestrictedToCommunity() {
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.COORD_PASTORAL_LOCAL, matrizId,
                    matrizId)).isTrue();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.COORD_PASTORAL_LOCAL, matrizId,
                    comunidadeBId)).isFalse();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, matrizId,
                    comunidadeBId)).isFalse();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.PRESIDENTE_CPC, matrizId,
                    comunidadeBId)).isFalse();
        }
    }
}
