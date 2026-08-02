package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    @Transactional
    public void execute(UUID userId) {
        UsuarioEntity user = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNotFoundException(userId));

        user.setEnabled(true);
        if (user.getRoles() == null || !user.getRoles().contains("ROLE_USER")) {
            if (user.getRoles() == null || user.getRoles().isBlank()) {
                user.setRoles("ROLE_USER");
            } else {
                user.setRoles(user.getRoles() + ",ROLE_USER");
            }
        }
        usuarioRepository.save(user);

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
