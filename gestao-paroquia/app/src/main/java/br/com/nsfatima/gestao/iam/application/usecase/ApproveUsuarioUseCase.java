package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Aprova um usuário pendente, habilitando seu acesso e ativando os vínculos organizacionais associados a
 * ele que ainda estejam pendentes. Enquanto não aprovado, o usuário existe no banco mas fica com
 * {@code enabled=false} e seus vínculos com {@code ativo=false}, ou seja, não consegue autenticar nem
 * aparece como membro efetivo de nenhuma organização.
 */
@Service
public class ApproveUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final MembroOrganizacaoJpaRepository vinculoRepository;

    public ApproveUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            MembroOrganizacaoJpaRepository vinculoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vinculoRepository = vinculoRepository;
    }

    /**
     * Habilita o usuário (permitindo login), garante que ele possua o papel padrão {@code ROLE_USER} — sem
     * duplicá-lo se já presente — e ativa todos os vínculos organizacionais ainda pendentes que ele possui,
     * tornando-o membro efetivo dessas organizações.
     *
     * @param userId identificador do usuário a aprovar
     * @throws UsuarioNotFoundException se não existir usuário com o id informado
     */
    @Transactional
    public void execute(UUID userId) {
        UsuarioEntity usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNotFoundException(userId));

        usuario.setEnabled(true);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        usuario.setAtualizadoPor(authentication != null ? authentication.getName() : null);
        if (usuario.getRoles() == null || !usuario.getRoles().contains("ROLE_USER")) {
            if (usuario.getRoles() == null || usuario.getRoles().isBlank()) {
                usuario.setRoles("ROLE_USER");
            } else {
                usuario.setRoles(usuario.getRoles() + ",ROLE_USER");
            }
        }
        usuarioRepository.save(usuario);

        // Activate requested vinculos
        var vinculos = vinculoRepository.findByUsuarioId(userId);
        for (var vinculo : vinculos) {
            if (!vinculo.isAtivo()) {
                vinculo.setAtivo(true);
                vinculoRepository.save(vinculo);
            }
        }
    }
}
