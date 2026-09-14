package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
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
public class AprovarUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final MembroGrupoJpaRepository vinculoRepository;

    public AprovarUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            MembroGrupoJpaRepository vinculoRepository) {
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

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        usuario.aprovar(authentication != null ? authentication.getName() : null);
        usuarioRepository.save(usuario);

        // Ativa os vínculos organizacionais pendentes
        if (usuario.getFielId() != null) {
            var vinculos = vinculoRepository.findByFielId(usuario.getFielId());
            for (var vinculo : vinculos) {
                if (!vinculo.isAtivo()) {
                    vinculo.setAtivo(true);
                    vinculoRepository.save(vinculo);
                }
            }
        }
    }
}
