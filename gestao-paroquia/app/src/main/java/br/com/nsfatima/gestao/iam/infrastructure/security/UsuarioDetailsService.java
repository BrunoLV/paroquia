package br.com.nsfatima.gestao.iam.infrastructure.security;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;
    private final ExternalMembershipReader externalMembershipReader;

    public UsuarioDetailsService(JdbcTemplate jdbcTemplate, ExternalMembershipReader externalMembershipReader) {
        this.jdbcTemplate = jdbcTemplate;
        this.externalMembershipReader = externalMembershipReader;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            List<UsuarioRecord> results = jdbcTemplate.query(
                    """
                            SELECT id, username, password_hash, enabled, roles
                            FROM calendario.usuarios
                            WHERE lower(username) = lower(?)
                            """,
                    this::mapUsuario,
                    username == null ? "" : username.trim());

            if (results.isEmpty()) {
                throw new UsernameNotFoundException("Usuario nao encontrado");
            }

            UsuarioRecord usuario = results.getFirst();
            return new UsuarioDetails(
                    usuario.id(),
                    usuario.username(),
                    usuario.passwordHash(),
                    usuario.enabled(),
                    usuario.roles(),
                    externalMembershipReader.findActiveMemberships(usuario.id()));
        } catch (DataAccessException ex) {
            throw new AuthenticationServiceException("Fonte de autorizacao indisponivel", ex);
        }
    }

    private UsuarioRecord mapUsuario(ResultSet rs, int rowNum) throws SQLException {
        return new UsuarioRecord(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getBoolean("enabled"),
                rs.getString("roles"));
    }

    private record UsuarioRecord(UUID id, String username, String passwordHash, boolean enabled, String roles) {
    }
}
