package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Lê, via SQL nativo (deliberadamente fora do JPA para evitar acoplar o contexto {@code iam} às entidades
 * do contexto {@code organizacao}), os vínculos organizacionais ativos de um usuário e os traduz em
 * authorities Spring Security no formato {@code ROLE_<TIPO_ORGANIZACAO>_<PAPEL>}. É a fonte de papéis
 * por-organização usada tanto no login ({@link UsuarioDetailsService}) quanto no perfil exposto pelo
 * {@code AuthController}.
 */
@Component
public class LeitorVinculoExterno {

    private final JdbcTemplate jdbcTemplate;

    public LeitorVinculoExterno(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Consulta todos os vínculos organizacionais do usuário que estejam ativos tanto no vínculo
     * ({@code m.ativo = TRUE}) quanto na organização vinculada ({@code o.ativo = TRUE}) — um vínculo com
     * organização desativada não é retornado, mesmo que o vínculo em si esteja ativo.
     *
     * @param userId identificador do usuário
     * @return lista de vínculos ativos, ordenada por tipo de organização, papel e id da organização
     */
    @Transactional(readOnly = true)
    public List<Vinculo> findVinculosAtivos(UUID userId) {
        return jdbcTemplate.query(
                """
                        SELECT m.organizacao_id,
                                m.tipo_organizacao, m.papel, m.igreja_vinculo_id, o.nome as organizacao_nome,
                                o.contato as organizacao_contato, o.coordenador as organizacao_coordenador,
                                o.vice_coordenador as organizacao_vice_coordenador, o.ativo as organizacao_ativo
                        
                        FROM calendario.membros_organizacao m
                        JOIN calendario.organizacoes o ON o.id = m.organizacao_id
                        WHERE m.usuario_id = ?
                          AND m.ativo = TRUE
                          AND o.ativo = TRUE
                        ORDER BY m.tipo_organizacao, m.papel, m.organizacao_id
                        """,
                this::mapVinculo,
                userId);
    }

    /**
     * Filtra as authorities do usuário para uma organização específica, ou retorna todas se
     * {@code organizationId} for {@code null}. Útil quando a checagem de autorização precisa saber apenas
     * os papéis do usuário dentro de um escopo organizacional já conhecido.
     *
     * @param userId         identificador do usuário
     * @param organizationId organização de interesse, ou {@code null} para não filtrar
     * @return conjunto ordenado (sem duplicatas) de authorities no formato {@code ROLE_<TIPO>_<PAPEL>}
     */
    @Transactional(readOnly = true)
    public Set<String> findRolesByUserAndOrganization(UUID userId, UUID organizationId) {
        Set<String> roles = new TreeSet<>();
        for (Vinculo vinculo : findVinculosAtivos(userId)) {
            if (organizationId == null || organizationId.equals(vinculo.organizationId())) {
                roles.add(vinculo.authority());
            }
        }
        return roles;
    }

    private Vinculo mapVinculo(ResultSet rs, int rowNum) throws SQLException {
        String igrejaVinculoStr = rs.getString("igreja_vinculo_id");
        UUID igrejaVinculoId = igrejaVinculoStr == null ? null : UUID.fromString(igrejaVinculoStr);
        return new Vinculo(
                UUID.fromString(rs.getString("organizacao_id")),
                rs.getString("organizacao_nome"),
                rs.getString("tipo_organizacao"),
                rs.getString("organizacao_contato"),
                rs.getString("organizacao_coordenador"),
                rs.getString("organizacao_vice_coordenador"),
                rs.getBoolean("organizacao_ativo"),
                rs.getString("papel"),
                igrejaVinculoId);
    }

    /**
     * Um vínculo ativo do usuário com uma organização, já carregando os dados de exibição da organização
     * (nome, contato, coordenadores) para evitar uma segunda consulta ao montar respostas de API.
     */
    public record Vinculo(
            UUID organizationId,
            String organizationName,
            String organizationType,
            String organizationContact,
            String organizationCoordinator,
            String organizationViceCoordinator,
            boolean organizationActive,
            String role,
            UUID igrejaVinculoId) {

        public Vinculo(UUID organizationId, String organizationType, String role) {
            this(organizationId, "Organizacao", organizationType, null, null, null, true, role, null);
        }

        private static String normalize(String value) {
            return value == null
                    ? "UNKNOWN"
                    : value.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        }

        /**
         * Constrói a authority Spring Security correspondente a este vínculo, no formato
         * {@code ROLE_<TIPO_ORGANIZACAO>_<PAPEL>} (ex.: {@code ROLE_COMUNIDADE_COORDENADOR}), normalizando
         * espaços e hífens para underscore em maiúsculas — é assim que papéis por-organização se tornam
         * comparáveis a {@code hasRole(...)}/{@code hasAuthority(...)} do Spring Security.
         *
         * @return a authority no formato {@code ROLE_<TIPO>_<PAPEL>}
         */
        public String authority() {
            return "ROLE_" + normalize(organizationType) + "_" + normalize(role);
        }
    }
}
