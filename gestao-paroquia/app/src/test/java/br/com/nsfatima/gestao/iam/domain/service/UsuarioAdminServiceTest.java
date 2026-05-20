package br.com.nsfatima.gestao.iam.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UsuarioAdminServiceTest {

    private UsuarioAdminService service;
    private UsuarioJpaRepository repository;
    private PasswordEncoder passwordEncoder;
    private AuditLogPersistenceService auditLogService;
    private UsuarioAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioJpaRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditLogService = mock(AuditLogPersistenceService.class);
        authorizationService = mock(UsuarioAuthorizationService.class);
        service = new UsuarioAdminService(repository, passwordEncoder, auditLogService, authorizationService);
    }

    @Test
    void shouldToggleUserStatus() {
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
    void shouldResetPassword() {
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
    void shouldUpdateRoles() {
        UUID id = UUID.randomUUID();
        UsuarioEntity user = new UsuarioEntity();
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.updateGlobalRoles(id, "ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", user.getRoles());
        verify(repository).save(user);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> service.toggleActiveStatus(id, true));
    }
}
