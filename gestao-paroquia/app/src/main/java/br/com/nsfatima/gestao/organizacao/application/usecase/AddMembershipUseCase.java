package br.com.nsfatima.gestao.organizacao.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddMembershipUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;
    private final UsuarioJpaRepository usuarioRepository;

    public AddMembershipUseCase(
            MembroOrganizacaoJpaRepository membershipRepository,
            UsuarioJpaRepository usuarioRepository) {
        this.membershipRepository = membershipRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UUID execute(UUID usuarioId, UUID organizacaoId, TipoOrganizacao tipo, PapelOrganizacional papel) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuario nao encontrado: " + usuarioId);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isParoco = auth.getAuthorities().stream().anyMatch(a -> "ROLE_CLERO_PAROCO".equals(a.getAuthority()));

        // Hierarchy rule: COORDENADOR and VICE of CONSELHO can only be defined by PAROCO or ADMIN
        if (tipo == TipoOrganizacao.CONSELHO && (papel == PapelOrganizacional.COORDENADOR || papel == PapelOrganizacional.VICE_COORDENADOR)) {
            if (!isAdmin && !isParoco) {
                throw new AccessDeniedException("Apenas o Pároco ou Admin podem definir coordenador e vice do Conselho");
            }
        }

        // Hierarchy rule: PAROCO can only be defined by ADMIN
        if (tipo == TipoOrganizacao.CLERO && papel == PapelOrganizacional.PAROCO) {
            if (!isAdmin) {
                throw new AccessDeniedException("Apenas o Admin pode definir o Pároco");
            }
        }

        // Unicity rule: Organizations can only have 1 coordinator and 1 vice-coordinator active at a time
        if (papel == PapelOrganizacional.COORDENADOR || papel == PapelOrganizacional.VICE_COORDENADOR) {
            membershipRepository.findByOrganizacaoIdAndPapelAndAtivoTrue(organizacaoId, papel.name())
                    .ifPresent(existing -> {
                        existing.setAtivo(false);
                        membershipRepository.save(existing);
                    });
        }

        MembroOrganizacaoEntity entity = new MembroOrganizacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setOrganizacaoId(organizacaoId);
        entity.setTipoOrganizacao(tipo.name());
        entity.setPapel(papel.name());
        entity.setAtivo(true);

        return membershipRepository.save(entity).getId();
    }
}
