package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioAuthorizationService {

    private final MembroOrganizacaoJpaRepository membershipRepository;

    public UsuarioAuthorizationService(MembroOrganizacaoJpaRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public void requireAdminOrCoordinatorOf(UUID targetUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        if (isAdmin(auth)) {
            return;
        }

        UUID actorId = getActorId(auth);
        if (actorId == null) {
            throw new AccessDeniedException("Actor ID not found");
        }

        // If actor is COORDENADOR, they must share an organization with targetUserId where they are COORDENADOR
        List<MembroOrganizacaoEntity> actorMemberships = membershipRepository.findByUsuarioIdAndAtivoTrue(actorId);
        List<UUID> coordinatorOrgIds = actorMemberships.stream()
                .filter(m -> "coordenador".equalsIgnoreCase(m.getPapel()) || "vice-coordenador".equalsIgnoreCase(m.getPapel()))
                .map(MembroOrganizacaoEntity::getOrganizacaoId)
                .toList();

        if (coordinatorOrgIds.isEmpty()) {
            throw new AccessDeniedException("Actor is not a coordinator");
        }

        List<MembroOrganizacaoEntity> targetMemberships = membershipRepository.findByUsuarioIdAndAtivoTrue(targetUserId);
        boolean sharesOrg = targetMemberships.stream()
                .anyMatch(m -> coordinatorOrgIds.contains(m.getOrganizacaoId()));

        if (!sharesOrg) {
            throw new AccessDeniedException("Coordinator can only manage members of their own organization");
        }
    }

    public void requireAdminOrCoordinatorOfOrganization(UUID organizationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        if (isAdmin(auth)) {
            return;
        }

        UUID actorId = getActorId(auth);
        if (actorId == null) {
            throw new AccessDeniedException("Actor ID not found");
        }

        boolean isCoordinator = membershipRepository.findByUsuarioIdAndAtivoTrue(actorId).stream()
                .filter(m -> organizationId.equals(m.getOrganizacaoId()))
                .anyMatch(m -> "coordenador".equalsIgnoreCase(m.getPapel()) || "vice-coordenador".equalsIgnoreCase(m.getPapel()));

        if (!isCoordinator) {
            throw new AccessDeniedException("Only Admin or Organization Coordinator can perform this action");
        }
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private UUID getActorId(Authentication auth) {
        if (auth.getPrincipal() instanceof UsuarioDetails details) {
            return details.getUsuarioId();
        }
        return null;
    }
}
