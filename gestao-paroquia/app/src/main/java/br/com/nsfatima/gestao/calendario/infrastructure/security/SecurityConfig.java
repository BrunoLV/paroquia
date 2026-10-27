package br.com.nsfatima.gestao.calendario.infrastructure.security;

import br.com.nsfatima.gestao.iam.infrastructure.security.LimitadorTentativas;
import br.com.nsfatima.gestao.iam.infrastructure.security.ResolvedorIpCliente;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;

/**
 * Cadeia de segurança HTTP da API (sessão por cookie, CORS, autorização por rota e cabeçalhos).
 *
 * <p>Decisões de endurecimento: (1) apenas {@code /actuator/health|info} e os endpoints de
 * autenticação/públicos são anônimos; métricas e documentação OpenAPI exigem ADMIN, e o scrape do
 * Prometheus usa um token Bearer dedicado restrito a {@code /actuator/prometheus}. (2) CSRF
 * tradicional segue desligado porque a API é JSON: a defesa em profundidade é o cookie de sessão
 * {@code SameSite=Lax} + {@code HttpOnly} + {@code Secure} (ver {@code application.yml}) e o
 * {@code CorsFilter}, que rejeita com 403 requisições mutáveis de origens não autorizadas.
 * (3) Cabeçalhos CSP, HSTS (apenas sobre HTTPS), Referrer-Policy e nosniff em todas as respostas.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String PAPEL_ADMIN = "ADMIN";
    private static final long HSTS_MAX_AGE_SEGUNDOS = 31_536_000L;
    private static final String CONTENT_SECURITY_POLICY = "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; "
            + "object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'";

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            DaoAuthenticationProvider daoAuthenticationProvider,
            JsonAuthenticationOutcomeProcessor authenticationOutcomeProcessor,
            ObjectMapper objectMapper,
            @Qualifier("limitadorLogin") LimitadorTentativas limitadorLogin,
            ResolvedorIpCliente resolvedorIp,
            @Value("${app.security.metrics-scrape-token:}") String metricsScrapeToken) {
        JsonUsernamePasswordAuthenticationFilter loginFilter =
                new JsonUsernamePasswordAuthenticationFilter(
                        authenticationManager, objectMapper, limitadorLogin, resolvedorIp);
        loginFilter.setAuthenticationSuccessHandler(authenticationOutcomeProcessor);
        loginFilter.setAuthenticationFailureHandler(authenticationOutcomeProcessor);

        http.authenticationProvider(daoAuthenticationProvider)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Sessao stateful mantida via JSESSIONID; CSRF desabilitado explicitamente para a API JSON.
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(HSTS_MAX_AGE_SEGUNDOS)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationOutcomeProcessor)
                        .accessDeniedHandler(authenticationOutcomeProcessor))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/prometheus")
                        .hasAnyRole(PAPEL_ADMIN, ActuatorScrapeTokenFilter.PAPEL_SCRAPER)
                        .requestMatchers("/actuator/**").hasRole(PAPEL_ADMIN)
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger",
                                "/swagger-ui",
                                "/swagger-ui/**",
                                "/swagger-ui.html").hasRole(PAPEL_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/autenticacao/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/autenticacao/solicitar-reset-senha").permitAll()
                        .requestMatchers("/api/v1/publico/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/organizacoes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/configuracao-paroquia").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/api/v1/configuracao-paroquia").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/configuracao-paroquia/logo/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/api/v1/configuracao-paroquia/logo/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/autenticacao/logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(authenticationOutcomeProcessor))
                .addFilterBefore(
                        new ActuatorScrapeTokenFilter(metricsScrapeToken), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization",
                "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers", "Idempotency-Key"));

        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials", "Content-Disposition"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
