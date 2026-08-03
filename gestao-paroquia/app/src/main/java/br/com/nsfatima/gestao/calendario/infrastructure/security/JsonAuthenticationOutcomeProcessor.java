package br.com.nsfatima.gestao.calendario.infrastructure.security;

import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.observabilidade.domain.service.CorrelationIdFilter;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduz os desfechos do fluxo de autenticação/autorização do Spring Security (sucesso, falha,
 * ausência de autenticação, acesso negado e logout) em respostas HTTP em formato JSON, em vez do
 * comportamento padrão do Spring Security baseado em redirecionamentos e páginas HTML de
 * login/erro. Isso é necessário porque o frontend é uma SPA que consome a API via JSON e precisa de
 * corpos de resposta estruturados (com código de erro, mensagem e correlation id) para tratar cada
 * desfecho de autenticação adequadamente.
 */
@Component
public class JsonAuthenticationOutcomeProcessor implements AuthenticationSuccessHandler,
        AuthenticationFailureHandler,
        AuthenticationEntryPoint,
        AccessDeniedHandler,
        LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationOutcomeProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Trata o sucesso da autenticação: estabelece o {@code SecurityContext} na sessão HTTP e
     * escreve na resposta, em JSON, os dados do usuário autenticado (id, username, papéis/roles e
     * organizações vinculadas) quando o principal for {@code UsuarioDetails}; caso contrário,
     * escreve apenas o nome de usuário.
     *
     * @param request requisição HTTP da tentativa de login
     * @param response resposta HTTP onde o corpo JSON de sucesso é escrito
     * @param authentication autenticação já validada pelo {@code AuthenticationManager}
     * @throws IOException se falhar a escrita do corpo JSON na resposta
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioDetails usuarioDetails) {
            objectMapper.writeValue(response.getOutputStream(), Map.of(
                    "usuarioId", usuarioDetails.getUsuarioId(),
                    "username", usuarioDetails.getUsername(),
                    "roles", usuarioDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()),
                    "organizations", usuarioDetails.getVinculos().stream()
                            .map(vinculo -> {
                                Map<String, Object> map = new java.util.HashMap<>();
                                map.put("id", vinculo.organizationId().toString());
                                map.put("nome", vinculo.organizationName());
                                map.put("tipo", vinculo.organizationType());
                                map.put("contato", vinculo.organizationContact());
                                map.put("ativo", vinculo.organizationActive());
                                return map;
                            })
                            .collect(Collectors.toList())));
            return;
        }

        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "username", authentication.getName()));
    }

    /**
     * Trata a falha da autenticação, escrevendo um corpo de erro JSON com o código apropriado ao
     * tipo de exceção: fonte de autorização indisponível ({@code AUTHZ_SOURCE_UNAVAILABLE}), usuário
     * com cadastro pendente de aprovação ({@code APROVACAO_OBRIGATORIA}), ou credenciais inválidas
     * ({@code AUTH_INVALID}) como caso genérico.
     *
     * @param request requisição HTTP da tentativa de login
     * @param response resposta HTTP onde o corpo JSON de erro é escrito
     * @param exception exceção de autenticação que motivou a falha
     * @throws IOException se falhar a escrita do corpo JSON na resposta
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        if (exception instanceof AuthenticationServiceException) {
            writeError(
                    response,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ErrorCodes.AUTHZ_SOURCE_UNAVAILABLE,
                    "Fonte de autorizacao temporariamente indisponivel.");
            return;
        }

        if (exception instanceof DisabledException) {
            writeError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    ErrorCodes.APROVACAO_OBRIGATORIA,
                    "Cadastro pendente de aprovacao por um administrador.");
            return;
        }

        writeError(
                response,
                HttpStatus.UNAUTHORIZED,
                ErrorCodes.AUTH_INVALID,
                "Nome de usuario ou senha incorretos.");
    }

    /**
     * Ponto de entrada acionado quando uma requisição não autenticada tenta acessar um recurso
     * protegido: escreve um erro JSON de 401, distinguindo entre sessão expirada
     * ({@code SESSION_EXPIRED}, quando há um cookie {@code JSESSIONID} ou um session id inválido)
     * e ausência completa de autenticação ({@code AUTH_REQUIRED}).
     *
     * @param request requisição HTTP que disparou a falta de autenticação
     * @param response resposta HTTP onde o corpo JSON de erro é escrito
     * @param authException exceção de autenticação associada à tentativa de acesso
     * @throws IOException se falhar a escrita do corpo JSON na resposta
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        boolean hasSessionCookie = request.getCookies() != null
                && Arrays.stream(request.getCookies())
                .anyMatch(cookie -> "JSESSIONID".equals(cookie.getName()));
        boolean sessionExpired = (request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid())
                || hasSessionCookie;
        writeError(
                response,
                HttpStatus.UNAUTHORIZED,
                sessionExpired ? ErrorCodes.SESSION_EXPIRED : ErrorCodes.AUTH_REQUIRED,
                sessionExpired ? "Sessao expirada. Faca login novamente." : "Autenticacao obrigatoria.");
    }

    /**
     * Trata o acesso negado a um recurso por um usuário já autenticado, escrevendo um erro JSON de
     * 403. Distingue o caso de papel/escopo organizacional incompatível
     * ({@link PapelEscopoInvalidoException}, código {@code ROLE_SCOPE_INVALID}) do acesso negado
     * genérico ({@code ACCESS_DENIED}).
     *
     * @param request requisição HTTP que teve o acesso negado
     * @param response resposta HTTP onde o corpo JSON de erro é escrito
     * @param accessDeniedException exceção que motivou a negação de acesso
     * @throws IOException se falhar a escrita do corpo JSON na resposta
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        ErrorCodes errorCode = accessDeniedException instanceof PapelEscopoInvalidoException
                ? ErrorCodes.ROLE_SCOPE_INVALID
                : ErrorCodes.ACCESS_DENIED;
        writeError(
                response,
                HttpStatus.FORBIDDEN,
                errorCode,
                accessDeniedException.getMessage() == null ? "Acesso negado." : accessDeniedException.getMessage());
    }

    /**
     * Trata o sucesso do logout, respondendo apenas com status 204 (sem corpo), já que não há
     * conteúdo relevante a devolver para uma SPA que consome a API em JSON.
     *
     * @param request requisição HTTP de logout
     * @param response resposta HTTP onde o status 204 é escrito
     * @param authentication autenticação que estava ativa antes do logout
     */
    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    private void writeError(
            HttpServletResponse response,
            HttpStatus status,
            ErrorCodes errorCode,
            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                new ValidationErrorResponse(
                        errorCode.name(),
                        message,
                        resolveCorrelationId(),
                        List.of(new ValidationErrorItem(errorCode.name(), "authorization", message, null))));
    }

    private String resolveCorrelationId() {
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
        return correlationId == null || correlationId.isBlank() ? "n/a" : correlationId;
    }
}
