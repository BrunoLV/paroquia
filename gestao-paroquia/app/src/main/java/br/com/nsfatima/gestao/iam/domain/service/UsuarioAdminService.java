package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class UsuarioAdminService {

    private static final String AUDIT_ACTOR = "admin";
    private static final String AUDIT_ACTION = "admin-action";
    private static final String AUDIT_TARGET = "user";
    private static final String AUDIT_RESULT = "success";
    private static final String KEY_RESOURCE_TYPE = "resourceType";
    private static final String VALUE_RESOURCE_TYPE = "USER";
    private static final String KEY_RESOURCE_ID = "resourceId";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ACTION = "action";
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

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, enabled ? "ENABLE" : "DISABLE"
        ));
    }

    @Transactional
    public void resetPassword(UUID id, String newRawPassword) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(user);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, "RESET_PASSWORD"
        ));
    }

    @Transactional
    public void updateGlobalRoles(UUID id, String roles) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity user = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        user.setRoles(roles);
        usuarioRepository.save(user);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, "UPDATE_ROLES",
                "roles", roles != null ? roles : ""
        ));
    }
}
