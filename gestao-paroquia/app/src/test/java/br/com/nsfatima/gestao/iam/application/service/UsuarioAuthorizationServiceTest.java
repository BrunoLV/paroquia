package br.com.nsfatima.gestao.iam.application.service;

import br.com.nsfatima.gestao.iam.domain.exception.GerenciamentoUsuarioNegadoException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.domain.policy.PadraoUsuarioGerenciamentoAutorizacaoPolicy;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioAuthorizationServiceTest {

    private PapelGlobalAuthorizationService papelGlobal;
    private UsuarioJpaRepository usuarioRepository;
    private UsuarioAuthorizationService service;
    private final UUID alvoId = UUID.randomUUID();
    private final UUID atorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        papelGlobal = mock(PapelGlobalAuthorizationService.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        service = new UsuarioAuthorizationService(mock(MembroGrupoJpaRepository.class), usuarioRepository,
                papelGlobal, new PadraoUsuarioGerenciamentoAutorizacaoPolicy());
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarCom(String roles) {
        UsuarioDetails details = new UsuarioDetails(atorId, "ator", "hash", true, roles, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    @Test
    void coordenadorNaoRedefineSenhaDeUsuarioDoGrupo() {
        autenticarCom("ROLE_COORDENADOR");

        assertThatThrownBy(() -> service.requirePodeRedefinirSenhaDe(alvoId))
                .isInstanceOf(GerenciamentoUsuarioNegadoException.class);
    }

    @Test
    void parocoNaoRedefineSenhaDeAlvoComPapelGlobal() {
        autenticarCom("ROLE_PAROCO");
        when(papelGlobal.isGlobalByUsuarioId(alvoId)).thenReturn(true);

        assertThatThrownBy(() -> service.requirePodeRedefinirSenhaDe(alvoId))
                .isInstanceOf(GerenciamentoUsuarioNegadoException.class);
    }

    @Test
    void parocoRedefineSenhaDeAlvoSemPapelGlobal() {
        autenticarCom("ROLE_PAROCO");
        when(papelGlobal.isGlobalByUsuarioId(alvoId)).thenReturn(false);

        assertThatCode(() -> service.requirePodeRedefinirSenhaDe(alvoId)).doesNotThrowAnyException();
    }

    @Test
    void adminRedefineSenhaDeAlvoComPapelGlobal() {
        autenticarCom("ROLE_ADMIN");
        when(papelGlobal.isGlobalByUsuarioId(alvoId)).thenReturn(true);

        assertThatCode(() -> service.requirePodeRedefinirSenhaDe(alvoId)).doesNotThrowAnyException();
    }

    @Test
    void usuarioSemAutenticacaoEhNegado() {
        assertThatThrownBy(() -> service.requirePodeRedefinirSenhaDe(alvoId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminPorPapelGlobalPersistidoDoFielTambemPassa() {
        autenticarCom("ROLE_USER");
        UsuarioEntity ator = new UsuarioEntity();
        UUID fielId = UUID.randomUUID();
        ator.setFielId(fielId);
        when(usuarioRepository.findById(atorId)).thenReturn(Optional.of(ator));
        when(papelGlobal.papeisGlobaisDe(fielId)).thenReturn(Set.of(PapelGlobal.ADMIN));
        when(papelGlobal.isGlobalByUsuarioId(alvoId)).thenReturn(true);

        assertThatCode(() -> service.requirePodeRedefinirSenhaDe(alvoId)).doesNotThrowAnyException();
    }
}
