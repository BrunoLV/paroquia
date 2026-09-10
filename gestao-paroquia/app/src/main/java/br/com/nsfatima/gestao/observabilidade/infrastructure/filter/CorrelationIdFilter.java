package br.com.nsfatima.gestao.observabilidade.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de borda executado com a maior precedência possível, responsável por garantir que
 * toda requisição HTTP tenha um correlationId disponível no {@link MDC} durante seu
 * processamento. Esse correlationId é o que {@code AuditoriaPersistenceService} propaga para
 * dentro dos registros de auditoria (chave "correlationId"), permitindo correlacionar todas as
 * operações auditáveis disparadas por uma mesma requisição, mesmo através de múltiplos
 * contextos de negócio. Também propaga um cabeçalho de idempotência opcional para uso por
 * casos de uso que precisem deduplicar reprocessamentos.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String IDEMPOTENCY_KEY = "idempotencyKey";

    /**
     * Lê (ou gera, se ausente) o correlationId da requisição, coloca-o no MDC junto com um
     * eventual Idempotency-Key, ecoa o correlationId de volta no header de resposta e garante
     * a limpeza do MDC ao final — mesmo em caso de exceção na cadeia de filtros — para evitar
     * vazamento de contexto entre requisições atendidas pela mesma thread.
     *
     * @param request requisição HTTP recebida
     * @param response resposta HTTP, no qual o correlationId é ecoado
     * @param filterChain cadeia de filtros a continuar
     * @throws ServletException se um filtro subsequente falhar
     * @throws IOException se ocorrer erro de I/O ao processar a requisição/resposta
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_KEY, correlationId);
        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            MDC.put(IDEMPOTENCY_KEY, idempotencyKey);
        }
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
            MDC.remove(IDEMPOTENCY_KEY);
        }
    }
}
