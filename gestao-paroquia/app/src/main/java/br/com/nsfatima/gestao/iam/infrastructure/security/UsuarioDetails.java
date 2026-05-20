package br.com.nsfatima.gestao.iam.infrastructure.security;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioDetails implements UserDetails {

    private final UUID usuarioId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final List<ExternalMembershipReader.Membership> memberships;
    private final List<GrantedAuthority> authorities;

    public UsuarioDetails(
            UUID usuarioId,
            String username,
            String password,
            boolean enabled,
            String roles,
            List<ExternalMembershipReader.Membership> memberships) {
        this.usuarioId = usuarioId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.memberships = memberships == null ? List.of() : List.copyOf(memberships);

        Stream<String> globalRoles = (roles == null || roles.isBlank())
                ? Stream.empty()
                : Arrays.stream(roles.split(",")).map(String::trim).filter(s -> !s.isEmpty());

        Stream<String> membershipRoles = this.memberships.stream()
                .map(ExternalMembershipReader.Membership::authority);

        this.authorities = Stream.concat(globalRoles, membershipRoles)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public List<ExternalMembershipReader.Membership> getMemberships() {
        return memberships;
    }

    public Optional<ExternalMembershipReader.Membership> primaryMembership() {
        return memberships.stream().findFirst();
    }

    public Optional<ExternalMembershipReader.Membership> findMembershipByOrganization(UUID organizationId) {
        return memberships.stream()
                .filter(membership -> organizationId != null && organizationId.equals(membership.organizationId()))
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
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
