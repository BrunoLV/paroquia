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
                        SELECT m.organizacao_id, m.tipo_organizacao, m.papel, o.nome as organizacao_nome, o.contato as organizacao_contato, o.coordenador as organizacao_coordenador, o.vice_coordenador as organizacao_vice_coordenador, o.ativo as organizacao_ativo
                        FROM calendario.membros_organizacao m
                        JOIN calendario.organizacoes o ON o.id = m.organizacao_id
                        WHERE m.usuario_id = ?
                          AND m.ativo = TRUE
                          AND o.ativo = TRUE
                        ORDER BY m.tipo_organizacao, m.papel, m.organizacao_id
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
                rs.getString("organizacao_nome"),
                rs.getString("tipo_organizacao"),
                rs.getString("organizacao_contato"),
                rs.getString("organizacao_coordenador"),
                rs.getString("organizacao_vice_coordenador"),
                rs.getBoolean("organizacao_ativo"),
                rs.getString("papel"));
    }

    public record Membership(
            UUID organizationId,
            String organizationName,
            String organizationType,
            String organizationContact,
            String organizationCoordinator,
            String organizationViceCoordinator,
            boolean organizationActive,
            String role) {

        public Membership(UUID organizationId, String organizationType, String role) {
            this(organizationId, "Organizacao", organizationType, null, null, null, true, role);
        }

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
