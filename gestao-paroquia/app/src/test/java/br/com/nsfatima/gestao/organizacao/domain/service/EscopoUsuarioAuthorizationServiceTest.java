package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
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
        service = new EscopoUsuarioAuthorizationService();
    }

    @Nested
    @DisplayName("ADMIN, PAROCO and VIGARIO Permissions")
    class AdminAndCleroGeneralPermissions {

        @Test
        @DisplayName("ADMIN has full global access for any action and target scope")
        void adminHasFullAccess() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.ADMIN,
                    TipoOrganizacao.PASTORAL, matrizId, matrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.ADMIN,
                    TipoOrganizacao.MATRIZ, matrizId, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.ADMIN,
                    TipoOrganizacao.PASTORAL, matrizId, comunidadeBId)).isTrue();

        }

        @Test
        @DisplayName("PAROCO and VIGARIO have full general management access")
        void parocoAndVigarioHaveFullGeneralAccess() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.PAROCO,
                    TipoOrganizacao.CLERO, null, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.VIGARIO,
                    TipoOrganizacao.CLERO, null, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.PAROCO,
                    TipoOrganizacao.CLERO, null, matrizId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.VIGARIO,
                    TipoOrganizacao.CLERO, null, comunidadeBId)).isTrue();

        }
    }

    @Nested
    @DisplayName("Conselho Pastoral Leadership Scope Permissions")
    class ConselhoPastoralScopePermissions {

        @Test
        @DisplayName("Conselho Pastoral Coordinator can manage data within their own community")
        void conselhoCoordinatorCommunityScope() {
            // Coordinator of Conselho Pastoral at Matriz can manage Matriz items and Pastoral at Matriz
            assertThat(service.canManageOrganizationData(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, matrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.MESA_CPP,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, pastoralMatrizId, matrizId)).isTrue();


            // But cannot manage items at Comunidade B
            assertThat(service.canManageOrganizationData(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, comunidadeBId, comunidadeBId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, pastoralComunidadeBId, comunidadeBId)).isFalse();

        }

        @Test
        @DisplayName("Conselho Pastoral Coordinator can approve events in their community")
        void conselhoCoordinatorCanApproveEventsInCommunity() {
            assertThat(service.canApproveEvents(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, matrizId)).isTrue();

            assertThat(service.canApproveEvents(PapelOrganizacional.PRESIDENTE_CPC,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, comunidadeBId)).isFalse();

        }
    }

    @Nested
    @DisplayName("Pastoral/Movimento Leadership Scope Permissions")
    class PastoralLeadershipScopePermissions {

        @Test
        @DisplayName("Pastoral Coordinator can only manage data for their exact Pastoral/Movimento")
        void pastoralCoordinatorExactPastoralScope() {
            // Coordinator of pastoralMatriz can only edit pastoralMatriz
            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, pastoralMatrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_MOVIMENTO,
                    TipoOrganizacao.MOVIMENTO, pastoralMatrizId, pastoralMatrizId, matrizId)).isTrue();


            // Cannot edit other pastorals or parent community
            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, pastoralComunidadeBId, comunidadeBId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, matrizId, matrizId)).isFalse();

        }

        @Test
        @DisplayName("Pastoral Coordinator cannot directly approve events (requires Conselho/Paroco aprovacao)")
        void pastoralCoordinatorCannotApproveEvents() {
            assertThat(service.canApproveEvents(PapelOrganizacional.COORD_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, pastoralMatrizId)).isFalse();

        }
    }

    @Nested
    @DisplayName("PADRE, SECRETARIO and VOLUNTARIO Scope Permissions")
    class ReadOnlyAndMemberPermissions {

        @Test
        @DisplayName("PADRE and SECRETARIO cannot perform general mutations, but can view and add observations")
        void padreAndSecretarioPermissions() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.PADRE,
                    TipoOrganizacao.CLERO, null, matrizId, matrizId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.SECRETARIO,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, matrizId, matrizId)).isFalse();


            assertThat(service.canAddObservation(PapelOrganizacional.PADRE,
                    TipoOrganizacao.CLERO, null, matrizId, false)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.SECRETARIO,
                    TipoOrganizacao.CONSELHO_PASTORAL, matrizId, comunidadeBId, false)).isTrue();

        }

        @Test
        @DisplayName("VOLUNTARIO can only add observations if event belongs"
                + " to their pastoral or pastoral is participant")
        void membroAddObservationPermissions() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, pastoralMatrizId, matrizId)).isFalse();


            // Can add observation if target org is their pastoral or they are participant
            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, pastoralMatrizId, false)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, comunidadeBId, true)).isTrue();


            // Cannot add observation to unrelated organization
            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                    TipoOrganizacao.PASTORAL, pastoralMatrizId, pastoralComunidadeBId, false)).isFalse();

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
