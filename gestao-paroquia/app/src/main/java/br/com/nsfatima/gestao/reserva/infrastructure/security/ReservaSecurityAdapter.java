package br.com.nsfatima.gestao.reserva.infrastructure.security;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolve o ator da requisição a partir do contexto de segurança. Fail-closed: sem usuário autenticado
 * (UsuarioDetails) o acesso é negado, nunca se assume um ator privilegiado.
 */
@Component
public class ReservaSecurityAdapter {

    public UsuarioActor obterAtorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioDetails userDetails)) {
            throw new AccessDeniedException("Operação de reserva exige um usuário autenticado.");
        }

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        List<UUID> coords = new ArrayList<>();
        List<UUID> membros = new ArrayList<>();

        if (userDetails.getVinculos() != null) {
            for (LeitorVinculoExterno.Vinculo v : userDetails.getVinculos()) {
                if (v.organizationId() != null) {
                    membros.add(v.organizationId());
                    String papel = v.role() != null ? v.role().toUpperCase() : "";
                    if ("COORDENADOR".equals(papel)) {
                        coords.add(v.organizationId());
                    }
                }
            }
        }

        return new UsuarioActor(
                userDetails.getUsuarioId(),
                userDetails.getUsername(),
                roles,
                coords,
                membros
        );
    }
}
