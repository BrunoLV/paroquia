package br.com.nsfatima.gestao.calendario.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Extensão do filtro padrão de login por usuário/senha do Spring Security que aceita o corpo da
 * requisição em JSON (além do tradicional {@code application/x-www-form-urlencoded}), pois o
 * frontend SPA envia as credenciais de login como um payload JSON em {@code POST
 * /api/v1/auth/login}. O desfecho (sucesso/falha) continua sendo delegado ao
 * {@link JsonAuthenticationOutcomeProcessor}, configurado como handler deste filtro.
 */
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public JsonUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/api/v1/autenticacao/login");
    }

    /**
     * Extrai usuário e senha da requisição de login e delega a autenticação ao
     * {@code AuthenticationManager}. Se o {@code Content-Type} da requisição for
     * {@code application/json}, o corpo é desserializado como {@link LoginRequest}; caso contrário,
     * usuário e senha são obtidos dos parâmetros de formulário padrão do Spring Security.
     *
     * @param request requisição HTTP de login (deve ser {@code POST})
     * @param response resposta HTTP associada à tentativa de autenticação
     * @return a autenticação resultante, avaliada pelo {@code AuthenticationManager}
     * @throws AuthenticationException se o método HTTP não for {@code POST}, se o payload JSON for
     *     inválido, ou se a autenticação em si falhar
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                LoginRequest body = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
                username = body.username();
                password = body.password();
            } catch (IOException ex) {
                throw new AuthenticationServiceException("Invalid login payload", ex);
            }
        }

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                username == null ? "" : username.trim(),
                password == null ? "" : password);
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private record LoginRequest(String username, String password) {
    }
}
