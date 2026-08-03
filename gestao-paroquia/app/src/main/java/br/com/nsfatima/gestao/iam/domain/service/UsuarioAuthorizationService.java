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

    private final MembroOrganizacaoJpaRepository vinculoRepository;

    public UsuarioAuthorizationService(MembroOrganizacaoJpaRepository vinculoRepository) {
        this.vinculoRepository = vinculoRepository;
    }

    public void requireAdminOrCoordinatorOf(UUID targetUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Não autenticado");
        }

        if (isAdmin(auth)) {
            return;
        }

        UUID actorId = getActorId(auth);
        if (actorId == null) {
            throw new AccessDeniedException("ID do ator não encontrado");
        }

        // If actor is COORDENADOR, they must share an organization with targetUserId where they are COORDENADOR
        List<MembroOrganizacaoEntity> actorVinculos = vinculoRepository.findByUsuarioIdAndAtivoTrue(actorId);
        List<UUID> coordinatorOrgIds = actorVinculos.stream()
                .filter(m -> isCoordinatorRole(m.getPapel()))
                .map(MembroOrganizacaoEntity::getOrganizacaoId)
                .toList();

        if (coordinatorOrgIds.isEmpty()) {
            throw new AccessDeniedException("Ator não é um coordenador");
        }

        List<MembroOrganizacaoEntity> targetVinculos =
                vinculoRepository.findByUsuarioIdAndAtivoTrue(targetUserId);
        boolean sharesOrg = targetVinculos.stream()
                .anyMatch(m -> coordinatorOrgIds.contains(m.getOrganizacaoId()));

        if (!sharesOrg) {
            throw new AccessDeniedException("Coordenador só pode gerenciar membros da própria organização");
        }
    }

    public void requireAdminOrCoordinatorOfOrganization(UUID organizationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Não autenticado");
        }

        if (isAdmin(auth)) {
            return;
        }

        UUID actorId = getActorId(auth);
        if (actorId == null) {
            throw new AccessDeniedException("ID do ator não encontrado");
        }

        boolean isCoordinator = vinculoRepository.findByUsuarioIdAndAtivoTrue(actorId).stream()
                .filter(m -> organizationId.equals(m.getOrganizacaoId()))
                .anyMatch(m -> isCoordinatorRole(m.getPapel()));

        if (!isCoordinator) {
            throw new AccessDeniedException("Apenas Admin ou Coordenador de Organização pode executar esta ação");
        }
    }

    private boolean isCoordinatorRole(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toLowerCase().replace("_", "-");
        return normalized.contains("coordenador")
                || normalized.contains("presidente")
                || normalized.contains("mesa-diretora");
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_CLERO_PAROCO".equals(a.getAuthority()));
    }

    private UUID getActorId(Authentication auth) {
        if (auth.getPrincipal() instanceof UsuarioDetails details) {
            return details.getUsuarioId();
        }
        return null;
    }
}
