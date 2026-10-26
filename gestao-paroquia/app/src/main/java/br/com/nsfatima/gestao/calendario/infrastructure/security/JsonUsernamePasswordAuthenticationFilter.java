package br.com.nsfatima.gestao.calendario.infrastructure.security;

import br.com.nsfatima.gestao.iam.infrastructure.security.LimitadorTentativas;
import br.com.nsfatima.gestao.iam.infrastructure.security.ResolvedorIpCliente;
import br.com.nsfatima.gestao.iam.infrastructure.security.TentativasExcessivasException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
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
 *
 * <p>Também aplica o limite de tentativas por IP + usuário: o usuário só é conhecido depois de
 * ler o corpo JSON, por isso o controle vive aqui e não em um filtro anterior (que consumiria o
 * corpo). Apenas credenciais inválidas contam; um login válido zera o contador.
 */
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;
    private final LimitadorTentativas limitador;
    private final ResolvedorIpCliente resolvedorIp;

    public JsonUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ObjectMapper objectMapper,
            LimitadorTentativas limitador,
            ResolvedorIpCliente resolvedorIp) {
        this.resolvedorIp = resolvedorIp;
        this.objectMapper = objectMapper;
        this.limitador = limitador;
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

        String chave = LimitadorTentativas.chave(resolvedorIp.resolver(request), username);
        if (limitador.bloqueado(chave)) {
            throw new TentativasExcessivasException(limitador.janela());
        }

        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                username == null ? "" : username.trim(),
                password == null ? "" : password);
        setDetails(request, authRequest);
        try {
            Authentication autenticacao = this.getAuthenticationManager().authenticate(authRequest);
            limitador.limpar(chave);
            return autenticacao;
        } catch (BadCredentialsException ex) {
            limitador.registrarTentativa(chave);
            throw ex;
        }
    }

    private record LoginRequest(String username, String password) {
    }
}
