package br.com.nsfatima.gestao.iam.application.service;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioAdminServiceTest {

    private UsuarioAdminService service;
    private UsuarioJpaRepository repository;
    private PasswordEncoder passwordEncoder;
    private AuditoriaPersistenceService auditLogService;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioJpaRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditLogService = mock(AuditoriaPersistenceService.class);
        UsuarioAuthorizationService authorizationService = mock(UsuarioAuthorizationService.class);
        service = new UsuarioAdminService(repository, passwordEncoder, auditLogService, authorizationService);
    }

    @Test
    void alternarStatusAtivo_deveAlternarUsuarioStatus() {
        UUID id = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEnabled(true);
        when(repository.findById(id)).thenReturn(Optional.of(usuario));

        service.alternarStatusAtivo(id, false);

        assertFalse(usuario.isEnabled());
        verify(repository).save(usuario);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void redefinirSenha_deveAtualizarHashSenhaELogarAuditoria() {
        UUID id = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("new")).thenReturn("hash-new");

        service.redefinirSenha(id, "new");

        assertEquals("hash-new", usuario.getPasswordHash());
        verify(repository).save(usuario);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void atualizarPapeisGlobais_deveAtualizarPapeisELogarAuditoria() {
        UUID id = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(usuario));

        service.atualizarPapeisGlobais(id, "ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", usuario.getRoles());
        verify(repository).save(usuario);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void alternarStatusAtivo_deveLancarUsuarioNaoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class, () -> service.alternarStatusAtivo(id, true));
    }
}
