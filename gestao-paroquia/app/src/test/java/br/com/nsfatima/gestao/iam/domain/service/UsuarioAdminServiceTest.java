package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
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
    void deveAlternarUsuarioStatus() {
        UUID id = UUID.randomUUID();
        UsuarioEntity user = new UsuarioEntity();
        user.setEnabled(true);
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.toggleActiveStatus(id, false);

        assertFalse(user.isEnabled());
        verify(repository).save(user);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveRedefinirSenha() {
        UUID id = UUID.randomUUID();
        UsuarioEntity user = new UsuarioEntity();
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new")).thenReturn("hash");

        service.resetPassword(id, "new");

        assertEquals("hash", user.getPasswordHash());
        verify(repository).save(user);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveAtualizarPapeis() {
        UUID id = UUID.randomUUID();
        UsuarioEntity user = new UsuarioEntity();
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.updateGlobalRoles(id, "ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", user.getRoles());
        verify(repository).save(user);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveLancarExceptionQuandoUsuarioNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> service.toggleActiveStatus(id, true));
    }
}
