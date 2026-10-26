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
                        SELECT m.organizacao_id as organizacao_id,
                                m.tipo_organizacao as tipo_organizacaoParoquial, m.papel, m.igreja_vinculo_id,
                                o.nome as organizacao_nome,
                                o.contato as organizacao_contato,
                                (SELECT f.nome FROM calendario.membro_grupo c
                                   JOIN calendario.fieis f ON f.id = c.fiel_id
                                  WHERE c.organizacao_id = o.id AND c.ativo AND c.papel = 'coordenador'
                                  LIMIT 1) as organizacao_coordenador,
                                (SELECT f.nome FROM calendario.membro_grupo c
                                   JOIN calendario.fieis f ON f.id = c.fiel_id
                                  WHERE c.organizacao_id = o.id AND c.ativo AND c.papel = 'secretario-grupo'
                                  LIMIT 1) as organizacao_vice_coordenador,
                                o.ativo as organizacao_ativo

                        FROM calendario.membro_grupo m
                        JOIN calendario.organizacao o ON o.id = m.organizacao_id
                        JOIN calendario.usuarios u ON u.fiel_id = m.fiel_id
                        WHERE u.id = ?
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
                roles.add(vinculo.scopedAuthority());
            }
        }
        return roles;
    }

    /**
     * Consulta os papéis globais ativos do Fiel do usuário informado (ver track
     * {@code roles_globais_vs_escopo}), retornando os códigos armazenados em {@code papel_global.papel}
     * (ex.: {@code "paroco"}, {@code "vigario"}) — o chamador normaliza para o formato de authority.
     *
     * @param userId identificador do usuário
     * @return lista de códigos de papel global ativos; vazia se o usuário não tiver nenhum
     */
    @Transactional(readOnly = true)
    public List<String> findPapeisGlobaisAtivos(UUID userId) {
        return jdbcTemplate.query(
                """
                        SELECT pg.papel
                        FROM calendario.papel_global pg
                        JOIN calendario.usuarios u ON u.fiel_id = pg.fiel_id
                        WHERE u.id = ?
                          AND pg.ativo = TRUE
                        UNION
                        SELECT CASE
                            WHEN ec.funcao = 'BISPO_DIOCESANO' THEN 'bispo-diocesano'
                            WHEN ec.funcao = 'PAROCO' THEN 'paroco'
                            WHEN ec.funcao = 'ADMINISTRADOR_PAROQUIAL' THEN 'paroco'
                            WHEN ec.funcao = 'VIGARIO_PAROQUIAL' THEN 'vigario'
                            WHEN ec.funcao = 'DIACONO_AUXILIAR' THEN 'diacono-paroquial'
                            WHEN ec.funcao = 'EMERITO' THEN 'fiel'
                            ELSE LOWER(REPLACE(ec.funcao, '_', '-'))
                        END as papel
                        FROM calendario.encargos_clericais ec
                        JOIN calendario.usuarios u ON u.clerigo_id = ec.clerigo_id
                        WHERE u.id = ?
                          AND ec.atual = TRUE
                        """,
                (rs, rowNum) -> rs.getString("papel"),
                userId,
                userId);
    }

    private Vinculo mapVinculo(ResultSet rs, int rowNum) throws SQLException {
        String igrejaVinculoStr = rs.getString("igreja_vinculo_id");
        UUID igrejaVinculoId = igrejaVinculoStr == null ? null : UUID.fromString(igrejaVinculoStr);
        return new Vinculo(
                UUID.fromString(rs.getString("organizacao_id")),
                rs.getString("organizacao_nome"),
                rs.getString("tipo_organizacaoParoquial"),
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
         * Constrói as authorities Spring Security correspondentes a este vínculo, gerando primariamente
         * o papel canônico puro {@code ROLE_<PAPEL>} (ex.: {@code ROLE_COORDENADOR}, {@code ROLE_SECRETARIO_CONSELHO})
         * e, secundariamente, o papel escopado {@code ROLE_<TIPO_GRUPO>_<PAPEL>} para compatibilidade.
         *
         * @return a authority principal canônica no formato {@code ROLE_<PAPEL>}
         */
        public String authority() {
            return "ROLE_" + normalize(role);
        }

        public String scopedAuthority() {
            return "ROLE_" + normalize(organizationType) + "_" + normalize(role);
        }
    }
}
