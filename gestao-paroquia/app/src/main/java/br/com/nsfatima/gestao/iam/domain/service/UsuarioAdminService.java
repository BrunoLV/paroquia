package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class UsuarioAdminService {

    private static final String AUDIT_ACTOR = "admin";
    private static final String AUDIT_ACTION = "acao-admin";
    private static final String AUDIT_TARGET = "usuario";
    private static final String AUDIT_RESULT = "sucesso";
    private static final String KEY_RESOURCE_TYPE = "resourceType";
    private static final String VALUE_RESOURCE_TYPE = "USUARIO";
    private static final String KEY_RESOURCE_ID = "resourceId";
    private static final String KEY_USER_ID = "usuarioId";
    private static final String KEY_ACTION = "action";
    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaPersistenceService auditLogService;
    private final UsuarioAuthorizationService authorizationService;
    public UsuarioAdminService(
            UsuarioJpaRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuditoriaPersistenceService auditLogService,
            UsuarioAuthorizationService authorizationService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public void toggleActiveStatus(UUID id, boolean enabled) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        usuario.setEnabled(enabled);
        usuarioRepository.save(usuario);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, enabled ? "ATIVAR" : "DESATIVAR"
        ));
    }

    @Transactional
    public void resetPassword(UUID id, String newRawPassword) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        usuario.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(usuario);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, "REDEFINIR_SENHA"
        ));
    }

    @Transactional
    public void updateGlobalRoles(UUID id, String roles) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        usuario.setRoles(roles);
        usuarioRepository.save(usuario);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, "ATUALIZAR_PAPEIS",
                "roles", roles != null ? roles : ""
        ));
    }
}
