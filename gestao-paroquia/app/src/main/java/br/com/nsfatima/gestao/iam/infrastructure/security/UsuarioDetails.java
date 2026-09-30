package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adaptação de {@code UsuarioEntity} para o contrato {@link UserDetails} do Spring Security, combinando os
 * papéis globais do usuário (persistidos como string separada por vírgulas) com as authorities derivadas de
 * cada vínculo organizacional ativo, num único conjunto de authorities sem duplicatas. É o principal
 * (retornado por {@link UsuarioDetailsService}) que fica disponível via
 * {@code @AuthenticationPrincipal} em toda a aplicação após o login.
 */
public class UsuarioDetails implements UserDetails {

    private final UUID usuarioId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final List<LeitorVinculoExterno.Vinculo> vinculos;
    private final List<GrantedAuthority> authorities;

    /**
     * Monta o conjunto final de authorities a partir de duas fontes: os papéis globais (string separada por
     * vírgulas, cada um normalizado com prefixo {@code ROLE_} se ainda não tiver) e as authorities derivadas
     * dos vínculos organizacionais ativos ({@link LeitorVinculoExterno.Vinculo#authority()}). Duplicatas
     * entre as duas fontes são eliminadas.
     *
     * @param usuarioId identificador do usuário
     * @param username  username/login
     * @param password  hash da senha (nunca texto puro)
     * @param enabled   se o usuário pode autenticar
     * @param roles     papéis globais separados por vírgula, ou {@code null}/vazio se nenhum
     * @param vinculos  vínculos organizacionais ativos do usuário, ou {@code null} tratado como lista vazia
     */
    public UsuarioDetails(
            UUID usuarioId,
            String username,
            String password,
            boolean enabled,
            String roles,
            List<LeitorVinculoExterno.Vinculo> vinculos) {
        this(usuarioId, username, password, enabled, roles, vinculos, List.of());
    }

    /**
     * Variante completa do construtor, incluindo os códigos de papel global ativos do Fiel do usuário (ver
     * track {@code roles_globais_vs_escopo}), traduzidos para authorities no formato
     * {@code ROLE_GLOBAL_<PAPEL>}.
     *
     * @param papeisGlobais códigos de papel global ativos (ex.: {@code "paroco"}), ou {@code null}/vazio
     */
    public UsuarioDetails(
            UUID usuarioId,
            String username,
            String password,
            boolean enabled,
            String roles,
            List<LeitorVinculoExterno.Vinculo> vinculos,
            List<String> papeisGlobais) {
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
                .flatMap(v -> Stream.of(v.authority(), v.scopedAuthority()));

        Stream<String> papelGlobalRoles = (papeisGlobais == null ? List.<String>of() : papeisGlobais).stream()
                .filter(p -> p != null && !p.isBlank())
                .flatMap(p -> {
                    String norm = p.trim().toUpperCase(Locale.ROOT).replace('-', '_');
                    return Stream.of("ROLE_" + norm, "ROLE_GLOBAL_" + norm);
                });

        this.authorities = Stream.concat(Stream.concat(globalRoles, vinculoRoles), papelGlobalRoles)
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

    /**
     * Retorna o primeiro vínculo organizacional ativo do usuário, sem garantia de ordem específica além da
     * ordenação já aplicada por {@link LeitorVinculoExterno#findVinculosAtivos}. Útil apenas quando o
     * chamador sabe, pelo contexto de negócio, que o usuário deve ter no máximo um vínculo relevante.
     *
     * @return o primeiro vínculo ativo, se houver algum
     */
    public Optional<LeitorVinculoExterno.Vinculo> vinculoPrimario() {
        return vinculos.stream().findFirst();
    }

    /**
     * Busca, entre os vínculos ativos do usuário, aquele referente a uma organização específica — usado
     * para resolver o papel do usuário dentro do escopo de uma organização em particular.
     *
     * @param organizationId organização de interesse
     * @return o vínculo do usuário com essa organização, se existir
     */
    public Optional<LeitorVinculoExterno.Vinculo> findVinculoPorGrupoParoquial(UUID organizationId) {
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
