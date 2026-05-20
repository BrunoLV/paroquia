package br.com.nsfatima.gestao.iam.infrastructure.security;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ExternalMembershipReader {

    private final JdbcTemplate jdbcTemplate;

    public ExternalMembershipReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<Membership> findActiveMemberships(UUID userId) {
        return jdbcTemplate.query(
                """
                        SELECT organizacao_id, tipo_organizacao, papel
                        FROM calendario.membros_organizacao
                        WHERE usuario_id = ?
                          AND ativo = TRUE
                        ORDER BY tipo_organizacao, papel, organizacao_id
                        """,
                this::mapMembership,
                userId);
    }

    @Transactional(readOnly = true)
    public Set<String> findRolesByUserAndOrganization(UUID userId, UUID organizationId) {
        Set<String> roles = new TreeSet<>();
        for (Membership membership : findActiveMemberships(userId)) {
            if (organizationId == null || organizationId.equals(membership.organizationId())) {
                roles.add(membership.authority());
            }
        }
        return roles;
    }

    private Membership mapMembership(ResultSet rs, int rowNum) throws SQLException {
        return new Membership(
                UUID.fromString(rs.getString("organizacao_id")),
                rs.getString("tipo_organizacao"),
                rs.getString("papel"));
    }

    public record Membership(UUID organizationId, String organizationType, String role) {
        public String authority() {
            return "ROLE_" + normalize(organizationType) + "_" + normalize(role);
        }

        private static String normalize(String value) {
            return value == null
                    ? "UNKNOWN"
                    : value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        }
    }
}
