package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.policy.PadraoAutorizacaoEscopoUsuarioPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EscopoUsuarioAuthorizationServiceTest {

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
        @DisplayName("ADMIN is barred from ecclesiastical governance and event approval")
        void adminBarredFromEcclesiasticalGovernance() {
            assertThat(service.canManageOrganizationData(PapelGlobal.ADMIN.storedValue(),
                    TipoGrupo.PASTORAL, matrizId, matrizId, matrizId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelGlobal.ADMIN.storedValue(),
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId, comunidadeBId)).isFalse();

            assertThat(service.canApproveEvents(PapelGlobal.ADMIN.storedValue(),
                    TipoGrupo.PASTORAL, matrizId, comunidadeBId)).isFalse();
        }

        @Test
        @DisplayName("PAROCO and VIGARIO have general management access, only PAROCO approves events")
        void parocoAndVigarioAccess() {
            assertThat(service.canManageOrganizationData(PapelGlobal.PAROCO.storedValue(),
                    TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelGlobal.VIGARIO.storedValue(),
                    TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();

            assertThat(service.canApproveEvents(PapelGlobal.PAROCO.storedValue(),
                    TipoGrupo.CONSELHO, null, matrizId)).isTrue();

            assertThat(service.canApproveEvents(PapelGlobal.VIGARIO.storedValue(),
                    TipoGrupo.CONSELHO, null, comunidadeBId)).isFalse();
        }
    }

    @Nested
    @DisplayName("Conselho Pastoral Scope Permissions")
    class ConselhoPastoralScopePermissions {

        @Test
        @DisplayName("Conselho Pastoral Secretary can manage council minutes within their own council")
        void conselhoSecretaryCommunityScope() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.SECRETARIO_CONSELHO.storedValue(),
                    TipoGrupo.CONSELHO, matrizId, matrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL.storedValue(),
                    TipoGrupo.CONSELHO, matrizId, pastoralMatrizId, matrizId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.SECRETARIO_CONSELHO.storedValue(),
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId, comunidadeBId)).isFalse();
        }

        @Test
        @DisplayName("Conselho Pastoral members cannot directly approve events (consultative vote only)")
        void conselhoMembersCannotApproveEvents() {
            assertThat(service.canApproveEvents(PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL.storedValue(),
                    TipoGrupo.CONSELHO, matrizId, matrizId)).isFalse();

            assertThat(service.canApproveEvents(PapelOrganizacional.SECRETARIO_CONSELHO.storedValue(),
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId)).isFalse();
        }
    }

    @Nested
    @DisplayName("Pastoral/Movimento Leadership Scope Permissions")
    class PastoralLeadershipScopePermissions {

        @Test
        @DisplayName("Coordinator can only manage data for their exact Pastoral/Movimento")
        void pastoralCoordinatorExactPastoralScope() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORDENADOR.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORDENADOR.storedValue(),
                    TipoGrupo.MOVIMENTO, pastoralMatrizId, pastoralMatrizId, matrizId)).isTrue();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORDENADOR.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralComunidadeBId, comunidadeBId)).isFalse();

            assertThat(service.canManageOrganizationData(PapelOrganizacional.COORDENADOR.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, matrizId, matrizId)).isFalse();
        }

        @Test
        @DisplayName("Coordinator cannot directly approve events (requires Paroco aprovacao)")
        void pastoralCoordinatorCannotApproveEvents() {
            assertThat(service.canApproveEvents(PapelOrganizacional.COORDENADOR.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId)).isFalse();
        }
    }

    @Nested
    @DisplayName("DIACONO_PAROQUIAL, SECRETARIO_CONSELHO and VOLUNTARIO Scope Permissions")
    class ReadOnlyAndMemberPermissions {

        @Test
        @DisplayName("DIACONO_PAROQUIAL and SECRETARIO_CONSELHO can view and add observations")
        void diaconoAndSecretarioPermissions() {
            assertThat(service.canManageOrganizationData(PapelGlobal.DIACONO_PAROQUIAL.storedValue(),
                    TipoGrupo.CONSELHO, null, matrizId, matrizId)).isFalse();

            assertThat(service.canAddObservation(PapelGlobal.DIACONO_PAROQUIAL.storedValue(),
                    TipoGrupo.CONSELHO, null, matrizId, false)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.SECRETARIO_CONSELHO.storedValue(),
                    TipoGrupo.CONSELHO, matrizId, comunidadeBId, false)).isTrue();
        }

        @Test
        @DisplayName("VOLUNTARIO can only add observations if event belongs to their pastoral or pastoral is participant")
        void membroAddObservationPermissions() {
            assertThat(service.canManageOrganizationData(PapelOrganizacional.VOLUNTARIO.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId, matrizId)).isFalse();

            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralMatrizId, false)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, comunidadeBId, true)).isTrue();

            assertThat(service.canAddObservation(PapelOrganizacional.VOLUNTARIO.storedValue(),
                    TipoGrupo.PASTORAL, pastoralMatrizId, pastoralComunidadeBId, false)).isFalse();
        }

        @Test
        @DisplayName("VOLUNTARIO and FIEL cannot view unapproved/pendente events")
        void membroCannotViewPendenteEvents() {
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.VOLUNTARIO.storedValue())).isFalse();
            assertThat(service.canViewPendenteEvents(PapelGlobal.FIEL.storedValue())).isFalse();
            assertThat(service.canViewPendenteEvents(PapelGlobal.DIACONO_PAROQUIAL.storedValue())).isTrue();
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.SECRETARIO_CONSELHO.storedValue())).isTrue();
            assertThat(service.canViewPendenteEvents(PapelOrganizacional.COORDENADOR.storedValue())).isTrue();
            assertThat(service.canViewPendenteEvents(PapelGlobal.PAROCO.storedValue())).isTrue();
            assertThat(service.canViewPendenteEvents(PapelGlobal.ADMIN.storedValue())).isTrue();
        }
    }

    @Nested
    @DisplayName("Territorial Scope Permissions")
    class TerritorialScopePermissions {

        @Test
        @DisplayName("Global clerical/pastoral roles have cross-community access")
        void globalRolesHaveCrossCommunityAccess() {
            assertThat(service.hasTerritorialAccess(
                    PapelGlobal.PAROCO.storedValue(), matrizId, comunidadeBId)).isTrue();
            assertThat(service.hasTerritorialAccess(
                    PapelGlobal.VIGARIO.storedValue(), matrizId, comunidadeBId)).isTrue();
            assertThat(service.hasTerritorialAccess(PapelGlobal.SECRETARIA_PAROQUIAL.storedValue(), matrizId,
                    comunidadeBId)).isTrue();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.COORDENADOR.storedValue(), null,
                    comunidadeBId)).isTrue();
        }

        @Test
        @DisplayName("Local organizational roles are restricted strictly to their community")
        void localRolesRestrictedToCommunity() {
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.COORDENADOR.storedValue(), matrizId,
                    matrizId)).isTrue();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.COORDENADOR.storedValue(), matrizId,
                    comunidadeBId)).isFalse();
            assertThat(service.hasTerritorialAccess(PapelOrganizacional.VOLUNTARIO.storedValue(), matrizId,
                    comunidadeBId)).isFalse();
            assertThat(service.hasTerritorialAccess(
                    PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL.storedValue(), matrizId, comunidadeBId)).isFalse();
        }
    }
}
