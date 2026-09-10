package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PadraoAutorizacaoEscopoUsuarioPolicyTest {

    private final PadraoAutorizacaoEscopoUsuarioPolicy policy = new PadraoAutorizacaoEscopoUsuarioPolicy();
    private final UUID matrizId = UUID.randomUUID();
    private final UUID comunidadeBId = UUID.randomUUID();

    @Test
    @DisplayName("Admin, Pároco e Vigário possuem acesso amplo de gestão")
    void devePermitirAcessoAmploParaCleroEAdmin() {
        assertThat(policy.canManageOrganizationData(PapelOrganizacional.ADMIN, TipoGrupo.CONSELHO, matrizId, comunidadeBId, comunidadeBId)).isTrue();
        assertThat(policy.canManageOrganizationData(PapelOrganizacional.PAROCO, TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();
        assertThat(policy.canManageOrganizationData(PapelOrganizacional.VIGARIO, TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();

        assertThat(policy.canApproveEvents(PapelOrganizacional.PAROCO, TipoGrupo.CONSELHO, null, matrizId)).isTrue();
    }

    @Test
    @DisplayName("Coordenadores só gerenciam a própria organização")
    void deveRestringirCoordenacaoAPropriaOrganizacao() {
        UUID orgId = UUID.randomUUID();
        UUID outraOrgId = UUID.randomUUID();

        assertThat(policy.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL, TipoGrupo.PASTORAL, orgId, orgId, matrizId)).isTrue();
        assertThat(policy.canManageOrganizationData(PapelOrganizacional.COORD_PASTORAL_LOCAL, TipoGrupo.PASTORAL, orgId, outraOrgId, matrizId)).isFalse();
    }

    @Test
    @DisplayName("Membros simples não podem visualizar eventos pendentes")
    void deveBloquearEventosPendentesParaMembros() {
        assertThat(policy.canViewPendenteEvents(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL)).isFalse();
        assertThat(policy.canViewPendenteEvents(PapelOrganizacional.MEMBRO_PASTORAL)).isFalse();
        assertThat(policy.canViewPendenteEvents(PapelOrganizacional.COORD_PASTORAL_LOCAL)).isTrue();
    }
}
