package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Cria uma conta de usuário no sistema, aplicando as diretrizes de governança eclesiástica (RN-IAM-05).
 * <p>
 * Quando executada por autoridade executiva ou técnica ({@code ADMIN} ou {@code PAROCO}), a conta nasce
 * imediatamente ativa ({@code enabled=true}). Quando submetida pela {@code SECRETARIA_PAROQUIAL}, exige
 * vínculo exclusivo com {@code Fiel}, nasce pendente ({@code enabled=false}) e dispara notificações
 * persistentes para os aprovadores ativos homologarem a liberação do acesso.
 */
@Service
public class CriarUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final FielJpaRepository fielRepository;
    private final ClerigoJpaRepository clerigoRepository;
    private final PasswordEncoder passwordEncoder;
    private final GerenciarNotificacaoUseCase notificacaoUseCase;

    public CriarUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            FielJpaRepository fielRepository,
            ClerigoJpaRepository clerigoRepository,
            PasswordEncoder passwordEncoder,
            GerenciarNotificacaoUseCase notificacaoUseCase) {
        this.usuarioRepository = usuarioRepository;
        this.fielRepository = fielRepository;
        this.clerigoRepository = clerigoRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificacaoUseCase = notificacaoUseCase;
    }

    @Transactional
    public UUID execute(String username, String rawPassword, UUID fielId) {
        return execute(username, rawPassword, fielId, null, false);
    }

    @Transactional
    public UUID execute(String username, String rawPassword, UUID fielId, UUID clerigoId) {
        return execute(username, rawPassword, fielId, clerigoId, false);
    }

    /**
     * Cria a conta de usuário, definindo seu status ativo ou pendente conforme a alçada de aprovação.
     *
     * @param username login único do usuário
     * @param rawPassword senha em texto plano a ser cifrada
     * @param fielId identificador da pessoa física leiga vinculada (se aplicável)
     * @param clerigoId identificador do ministro ordenado vinculado (se aplicável)
     * @param pendenteAprovacao verdadeiro se a criação foi solicitada por perfil que exige homologação posterior
     * @return identificador único do usuário persistido
     */
    @Transactional
    public UUID execute(String username, String rawPassword, UUID fielId, UUID clerigoId, boolean pendenteAprovacao) {
        if ((fielId == null && clerigoId == null) || (fielId != null && clerigoId != null)) {
            throw new IllegalArgumentException("Usuario deve estar vinculado exclusivamente a um Fiel ou a um Clerigo");
        }

        if (pendenteAprovacao && clerigoId != null) {
            throw new IllegalArgumentException(
                    "Usuarios pendentes de aprovacao criados pela secretaria devem estar vinculados "
                            + "exclusivamente a um Fiel");
        }

        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username ja existe: " + username);
        }

        Optional<FielEntity> fielOptional = fielId != null ? fielRepository.findById(fielId) : Optional.empty();
        if (fielId != null && fielOptional.isEmpty()) {
            throw new IllegalArgumentException("Fiel nao encontrado: " + fielId);
        }

        if (clerigoId != null && clerigoRepository.findById(clerigoId).isEmpty()) {
            throw new IllegalArgumentException("Clerigo nao encontrado: " + clerigoId);
        }

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setFielId(fielId);
        entity.setClerigoId(clerigoId);
        entity.setEnabled(!pendenteAprovacao);
        if (fielId != null) {
            entity.setRoles("ROLE_FIEL");
        } else if (clerigoId != null) {
            entity.setRoles("ROLE_CLERO");
        }

        UUID savedId = usuarioRepository.save(entity).getId();

        if (pendenteAprovacao && notificacaoUseCase != null) {
            String nomeFiel = fielOptional.map(FielEntity::getNome).orElse("Fiel");
            String mensagem = "Nova solicitação de aprovação de usuário: '" + username
                    + "' vinculado ao fiel '" + nomeFiel + "' aguarda homologação do Pároco ou Administrador.";
            var aprovadores = usuarioRepository.findAprovadoresAtivos();
            for (var aprovador : aprovadores) {
                notificacaoUseCase.publicar(aprovador.getId(), "USUARIO_APROVACAO_PENDENTE", mensagem);
            }
        }

        return savedId;
    }
}
