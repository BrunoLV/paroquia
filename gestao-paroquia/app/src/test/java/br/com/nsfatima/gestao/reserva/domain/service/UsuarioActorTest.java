package br.com.nsfatima.gestao.reserva.domain.service;

import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioActorTest {

    @Test
    @DisplayName("isAdminOuParoco retorna false quando roles é nulo")
    void isAdminOuParocoFalseQuandoRolesNulo() {
        UsuarioActor ator = new UsuarioActor(UUID.randomUUID(), "x", null, List.of(), List.of());
        assertThat(ator.isAdminOuParoco()).isFalse();
    }

    @Test
    @DisplayName("isAdminOuParoco retorna true quando possui role administrativa")
    void isAdminOuParocoTrueParaRoleAdministrativa() {
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_PAROCO"), List.of(), List.of());
        assertThat(ator.isAdminOuParoco()).isTrue();
    }

    @Test
    @DisplayName("isAdminOuParoco retorna false para role comum")
    void isAdminOuParocoFalseParaRoleComum() {
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_FIEL"), List.of(), List.of());
        assertThat(ator.isAdminOuParoco()).isFalse();
    }

    @Test
    @DisplayName("isCoordenadorDaOrganizacao retorna true para admin independentemente da organização")
    void isCoordenadorTrueParaAdmin() {
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ADMIN"), List.of(), List.of());
        assertThat(ator.isCoordenadorDaOrganizacao(UUID.randomUUID())).isTrue();
        assertThat(ator.isCoordenadorDaOrganizacao(null)).isTrue();
    }

    @Test
    @DisplayName("isCoordenadorDaOrganizacao retorna false quando orgId é nulo e não é admin")
    void isCoordenadorFalseQuandoOrgIdNulo() {
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_FIEL"), List.of(), List.of());
        assertThat(ator.isCoordenadorDaOrganizacao(null)).isFalse();
    }

    @Test
    @DisplayName("isCoordenadorDaOrganizacao retorna false quando lista de coordenação é nula")
    void isCoordenadorFalseQuandoListaNula() {
        UsuarioActor ator = new UsuarioActor(UUID.randomUUID(), "x", List.of("ROLE_FIEL"), null, List.of());
        assertThat(ator.isCoordenadorDaOrganizacao(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("isCoordenadorDaOrganizacao retorna true quando organizacao está na lista de coordenação")
    void isCoordenadorTrueQuandoNaLista() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_FIEL"), List.of(orgId), List.of());
        assertThat(ator.isCoordenadorDaOrganizacao(orgId)).isTrue();
        assertThat(ator.isCoordenadorDaOrganizacao(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("isMembroDaOrganizacao retorna true para admin independentemente da organização")
    void isMembroTrueParaAdmin() {
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_ADMIN"), List.of(), List.of());
        assertThat(ator.isMembroDaOrganizacao(UUID.randomUUID())).isTrue();
    }

    @Test
    @DisplayName("isMembroDaOrganizacao retorna false quando orgId nulo e não é admin")
    void isMembroFalseQuandoOrgIdNulo() {
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_FIEL"), List.of(), List.of());
        assertThat(ator.isMembroDaOrganizacao(null)).isFalse();
    }

    @Test
    @DisplayName("isMembroDaOrganizacao retorna true quando é coordenador ou membro da organização")
    void isMembroTrueQuandoCoordOuMembro() {
        UUID orgCoord = UUID.randomUUID();
        UUID orgMembro = UUID.randomUUID();
        UUID orgNenhuma = UUID.randomUUID();
        UsuarioActor ator = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_FIEL"), List.of(orgCoord), List.of(orgMembro));

        assertThat(ator.isMembroDaOrganizacao(orgCoord)).isTrue();
        assertThat(ator.isMembroDaOrganizacao(orgMembro)).isTrue();
        assertThat(ator.isMembroDaOrganizacao(orgNenhuma)).isFalse();
    }

    @Test
    @DisplayName("isMembroDaOrganizacao lida com listas nulas de coordenação e membresia")
    void isMembroComListasNulas() {
        UsuarioActor ator = new UsuarioActor(UUID.randomUUID(), "x", List.of("ROLE_FIEL"), null, null);
        assertThat(ator.isMembroDaOrganizacao(UUID.randomUUID())).isFalse();
    }
}
