package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Ponto de integração do Spring Security com a base de usuários: localiza a conta pelo username (via SQL
 * nativo, não JPA, para não acoplar a autenticação ao ciclo de vida de entidades) e monta o
 * {@link UsuarioDetails} completo, já enriquecido com os vínculos organizacionais ativos resolvidos por
 * {@link LeitorVinculoExterno}. Chamado automaticamente pelo Spring Security a cada tentativa de login.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;
    private final LeitorVinculoExterno externalVinculoReader;

    public UsuarioDetailsService(JdbcTemplate jdbcTemplate, LeitorVinculoExterno externalVinculoReader) {
        this.jdbcTemplate = jdbcTemplate;
        this.externalVinculoReader = externalVinculoReader;
    }

    /**
     * Busca o usuário pelo username (case-insensitive) e monta seu {@link UserDetails} com papéis globais e
     * authorities de vínculos organizacionais já resolvidos. Erros de acesso a dados são traduzidos para
     * {@link AuthenticationServiceException} para não vazar detalhes de infraestrutura no fluxo de
     * autenticação.
     *
     * @param username username informado no login, comparado ignorando maiúsculas/minúsculas
     * @return os detalhes completos do usuário autenticável
     * @throws UsernameNotFoundException      se não existir usuário com esse username
     * @throws AuthenticationServiceException se a consulta ao banco falhar (indisponibilidade da fonte de
     *                                         dados de autenticação/autorização)
     */
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
                    externalVinculoReader.findVinculosAtivos(usuario.id()));
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
