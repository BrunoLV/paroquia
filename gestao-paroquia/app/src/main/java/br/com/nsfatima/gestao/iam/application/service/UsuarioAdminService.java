package br.com.nsfatima.gestao.iam.application.service;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException;
import br.com.nsfatima.gestao.iam.domain.model.Papeis;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Executa as ações administrativas sensíveis sobre contas de usuário (ativar/desativar, redefinir senha,
 * alterar papéis globais), reaplicando a checagem de autorização de escopo (admin global ou coordenador da
 * organização do usuário-alvo, via {@link UsuarioAuthorizationService}) independentemente do que o
 * controller já validou, e registrando cada ação na trilha de auditoria
 * ({@link AuditoriaPersistenceService}) para rastreabilidade de quem alterou o quê.
 */
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

    /**
     * Ativa ou desativa o acesso de um usuário, registrando a ação na auditoria. Usuário desativado não
     * consegue mais autenticar, mesmo com credenciais válidas.
     *
     * @param id      identificador do usuário alvo
     * @param enabled novo estado de habilitação
     * @throws org.springframework.security.access.AccessDeniedException se o ator não for admin global nem
     *                                                                    coordenador de uma organização do usuário-alvo
     * @throws br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException se o usuário não existir
     */
    @Transactional
    public void alternarStatusAtivo(UUID id, boolean enabled) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        if (enabled) {
            usuario.ativar();
        } else {
            usuario.desativar();
        }
        usuarioRepository.save(usuario);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, enabled ? "ATIVAR" : "DESATIVAR"
        ));
    }

    /**
     * Redefine administrativamente a senha de um usuário (sem exigir a senha atual), registrando a ação na
     * auditoria. Usado quando o usuário perdeu acesso à conta ou por política de segurança.
     *
     * @param id             identificador do usuário alvo
     * @param newRawPassword nova senha em texto puro, criptografada antes da persistência
     * @throws org.springframework.security.access.AccessDeniedException se o ator não for admin global nem
     *                                                                    coordenador de uma organização do usuário-alvo
     * @throws br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException se o usuário não existir
     */
    @Transactional
    public void redefinirSenha(UUID id, String newRawPassword) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        usuario.redefinirSenhaHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(usuario);

        auditLogService.log(AUDIT_ACTOR, AUDIT_ACTION, AUDIT_TARGET, AUDIT_RESULT, Map.of(
                KEY_RESOURCE_TYPE, VALUE_RESOURCE_TYPE,
                KEY_RESOURCE_ID, id,
                KEY_USER_ID, id,
                KEY_ACTION, "REDEFINIR_SENHA"
        ));
    }

    /**
     * Substitui integralmente os papéis (roles) globais de um usuário, registrando a ação e o novo conjunto
     * de papéis na auditoria. Papéis globais valem no sistema inteiro, diferente de papéis por vínculo
     * organizacional.
     *
     * @param id    identificador do usuário alvo
     * @param roles nova string de papéis separados por vírgula, que substitui os papéis globais atuais
     * @throws org.springframework.security.access.AccessDeniedException se o ator não for admin global nem
     *                                                                    coordenador de uma organização do usuário-alvo
     * @throws br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException se o usuário não existir
     */
    @Transactional
    public void atualizarPapeisGlobais(UUID id, String roles) {
        authorizationService.requireAdminOrCoordinatorOf(id);

        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        usuario.substituirPapeis(Papeis.deCsv(roles));
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
