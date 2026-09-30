package br.com.nsfatima.gestao.organizacao.domain.policy;

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
    @DisplayName("Pároco e Vigário possuem acesso amplo de gestão enquanto Admin é isolado")
    void devePermitirAcessoAmploParaCleroEBloquearAdmin() {
        assertThat(policy.canManageOrganizationData(
                "admin", TipoGrupo.CONSELHO, matrizId, comunidadeBId, comunidadeBId)).isFalse();
        assertThat(policy.canManageOrganizationData(
                "paroco", TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();
        assertThat(policy.canManageOrganizationData(
                "vigario", TipoGrupo.CONSELHO, null, comunidadeBId, comunidadeBId)).isTrue();

        assertThat(policy.canApproveEvents("paroco", TipoGrupo.CONSELHO, null, matrizId)).isTrue();
    }

    @Test
    @DisplayName("Coordenadores só gerenciam a própria organização")
    void deveRestringirCoordenacaoAPropriaOrganizacao() {
        UUID orgId = UUID.randomUUID();
        UUID outraOrgId = UUID.randomUUID();

        assertThat(policy.canManageOrganizationData(
                "coordenador", TipoGrupo.PASTORAL, orgId, orgId, matrizId)).isTrue();
        assertThat(policy.canManageOrganizationData(
                "coordenador", TipoGrupo.PASTORAL, orgId, outraOrgId, matrizId)).isFalse();
    }

    @Test
    @DisplayName("Membros simples não podem visualizar eventos pendentes")
    void deveBloquearEventosPendentesParaMembros() {
        assertThat(policy.canViewPendenteEvents("voluntario")).isFalse();
        assertThat(policy.canViewPendenteEvents("membro-ativo")).isFalse();
        assertThat(policy.canViewPendenteEvents("coordenador")).isTrue();
    }
}
