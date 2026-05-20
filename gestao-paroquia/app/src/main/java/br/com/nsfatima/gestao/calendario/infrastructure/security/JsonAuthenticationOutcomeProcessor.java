package br.com.nsfatima.gestao.calendario.infrastructure.security;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import br.com.nsfatima.gestao.observabilidade.domain.service.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

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
                    "username", usuarioDetails.getUsername()));
            return;
        }

        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "username", authentication.getName()));
    }

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

        writeError(
                response,
                HttpStatus.UNAUTHORIZED,
                ErrorCodes.AUTH_INVALID,
                "Nome de usuario ou senha incorretos.");
    }

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

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        ErrorCodes errorCode = accessDeniedException instanceof RoleScopeInvalidException
                ? ErrorCodes.ROLE_SCOPE_INVALID
                : ErrorCodes.ACCESS_DENIED;
        writeError(
                response,
                HttpStatus.FORBIDDEN,
                errorCode,
                accessDeniedException.getMessage() == null ? "Acesso negado." : accessDeniedException.getMessage());
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
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
