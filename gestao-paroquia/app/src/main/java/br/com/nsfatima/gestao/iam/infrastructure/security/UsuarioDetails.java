package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsuarioDetails implements UserDetails {

    private final UUID usuarioId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final List<LeitorVinculoExterno.Vinculo> vinculos;
    private final List<GrantedAuthority> authorities;

    public UsuarioDetails(
            UUID usuarioId,
            String username,
            String password,
            boolean enabled,
            String roles,
            List<LeitorVinculoExterno.Vinculo> vinculos) {
        this.usuarioId = usuarioId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.vinculos = vinculos == null ? List.of() : List.copyOf(vinculos);

        Stream<String> globalRoles = (roles == null || roles.isBlank())
                ? Stream.empty()
                : Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r);

        Stream<String> vinculoRoles = this.vinculos.stream()
                .map(LeitorVinculoExterno.Vinculo::authority);

        this.authorities = Stream.concat(globalRoles, vinculoRoles)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public List<LeitorVinculoExterno.Vinculo> getVinculos() {
        return vinculos;
    }

    public Optional<LeitorVinculoExterno.Vinculo> vinculoPrimario() {
        return vinculos.stream().findFirst();
    }

    public Optional<LeitorVinculoExterno.Vinculo> findVinculoPorOrganizacao(UUID organizationId) {
        return vinculos.stream()
                .filter(vinculo -> organizationId != null && organizationId.equals(vinculo.organizationId()))
                .findFirst();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
