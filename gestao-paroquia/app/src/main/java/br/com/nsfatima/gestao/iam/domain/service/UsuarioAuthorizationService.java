package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Checagem fina de autorização para operações administrativas sobre usuários: além do papel global exigido
 * pelo {@code @PreAuthorize} do endpoint, garante que um ator com papel de coordenador só possa agir sobre
 * usuários/organizações dentro do seu próprio escopo organizacional — admin global sempre passa
 * incondicionalmente. Lança {@link AccessDeniedException} (não retorna booleano) porque o chamador espera
 * interromper o fluxo imediatamente quando a autorização falha.
 */
@Service
public class UsuarioAuthorizationService {

    private final MembroGrupoJpaRepository vinculoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final PapelGlobalAuthorizationService papelGlobalAuthorizationService;

    public UsuarioAuthorizationService(MembroGrupoJpaRepository vinculoRepository,
                                        UsuarioJpaRepository usuarioRepository,
                                        PapelGlobalAuthorizationService papelGlobalAuthorizationService) {
        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.papelGlobalAuthorizationService = papelGlobalAuthorizationService;
    }

    /**
     * Garante que o ator autenticado pode gerenciar o usuário-alvo: passa livre se for admin global; caso
     * contrário, exige que o ator seja coordenador (papel contendo "coordenador", "presidente" ou
     * "mesa-diretora", normalizado) em pelo menos uma organização onde o usuário-alvo também possui vínculo
     * ativo — ou seja, coordenador só gerencia membros da(s) própria(s) organização(ões).
     *
     * @param targetUserId identificador do usuário sobre o qual a ação será executada
     * @throws AccessDeniedException se não houver autenticação, se o ator não for admin nem coordenador, ou
     *                               se o ator for coordenador mas não compartilhar organização com o alvo
     */
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

        UUID actorFielId = resolveFielId(actorId);
        UUID targetFielId = resolveFielId(targetUserId);

        // If actor is COORDENADOR, they must share an organization with targetUserId where they are COORDENADOR
        List<MembroGrupoEntity> actorVinculos = actorFielId == null
                ? List.of() : vinculoRepository.findByFielIdAndAtivoTrue(actorFielId);
        List<UUID> coordinatorOrgIds = actorVinculos.stream()
                .filter(m -> isCoordinatorRole(m.getPapel()))
                .map(MembroGrupoEntity::getOrganizacaoId)
                .toList();

        if (coordinatorOrgIds.isEmpty()) {
            throw new AccessDeniedException("Ator não é um coordenador");
        }

        List<MembroGrupoEntity> targetVinculos = targetFielId == null
                ? List.of() : vinculoRepository.findByFielIdAndAtivoTrue(targetFielId);
        boolean sharesOrg = targetVinculos.stream()
                .anyMatch(m -> coordinatorOrgIds.contains(m.getOrganizacaoId()));

        if (!sharesOrg) {
            throw new AccessDeniedException("Coordenador só pode gerenciar membros da própria organização");
        }
    }

    /**
     * Garante que o ator autenticado pode agir em nome de uma organização específica (ex.: adicionar vínculo
     * de membro): passa livre se for admin global; caso contrário, exige que o ator seja coordenador
     * (vínculo ativo com papel de coordenação) especificamente naquela organização — diferente de
     * {@link #requireAdminOrCoordinatorOf(UUID)}, aqui a organização é conhecida de antemão em vez de
     * derivada dos vínculos de um usuário-alvo.
     *
     * @param organizationId identificador da organização sobre a qual a ação será executada
     * @throws AccessDeniedException se não houver autenticação, ou se o ator não for admin global nem
     *                               coordenador ativo daquela organização
     */
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

        UUID actorFielId = resolveFielId(actorId);
        boolean isCoordinator = actorFielId != null && vinculoRepository.findByFielIdAndAtivoTrue(actorFielId)
                .stream()
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
        boolean adminPorAuthority = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (adminPorAuthority) {
            return true;
        }
        UUID actorId = getActorId(auth);
        return actorId != null && papelGlobalAuthorizationService.isGlobalByUsuarioId(actorId);
    }

    private UUID resolveFielId(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId).map(u -> u.getFielId()).orElse(null);
    }

    private UUID getActorId(Authentication auth) {
        if (auth.getPrincipal() instanceof UsuarioDetails details) {
            return details.getUsuarioId();
        }
        return null;
    }
}
