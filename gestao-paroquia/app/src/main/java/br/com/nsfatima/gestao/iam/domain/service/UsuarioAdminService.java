package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class UsuarioAdminService {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogPersistenceService auditLogService;
    private final UsuarioAuthorizationService authorizationService;

    public UsuarioAdminService(
            UsuarioJpaRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuditLogPersistenceService auditLogService,
            UsuarioAuthorizationService authorizationService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public void toggleActiveStatus(UUID id, boolean enabled) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setEnabled(enabled);
        usuarioRepository.save(user);

        auditLogService.log("admin", "admin-action", "user", "success", Map.of(
                "resourceType", "USER",
                "resourceId", id,
                "userId", id,
                "action", enabled ? "ENABLE" : "DISABLE"
        ));
    }

    @Transactional
    public void resetPassword(UUID id, String newRawPassword) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(user);

        auditLogService.log("admin", "admin-action", "user", "success", Map.of(
                "resourceType", "USER",
                "resourceId", id,
                "userId", id,
                "action", "PASSWORD_RESET"
        ));
    }

    @Transactional
    public void updateGlobalRoles(UUID id, String roles) {
        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setRoles(roles);
        usuarioRepository.save(user);

        auditLogService.log("admin", "admin-action", "user", "success", Map.of(
                "resourceType", "USER",
                "resourceId", id,
                "userId", id,
                "action", "UPDATE_ROLES",
                "roles", roles
        ));
    }
}
