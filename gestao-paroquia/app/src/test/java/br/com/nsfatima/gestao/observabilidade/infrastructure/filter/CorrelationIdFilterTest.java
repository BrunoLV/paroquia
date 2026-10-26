package br.com.nsfatima.gestao.observabilidade.infrastructure.filter;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    private String ecoado(String valorRecebido) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (valorRecebido != null) {
            request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, valorRecebido);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
    }

    @Test
    @DisplayName("Deve preservar correlation id em formato seguro")
    void devePreservarIdSeguro() throws Exception {
        assertThat(ecoado("abc-123_X.9")).isEqualTo("abc-123_X.9");
    }

    @Test
    @DisplayName("Deve substituir por UUID ids com caracteres de controle, espaços ou tamanho excessivo")
    void deveSubstituirIdInseguro() throws Exception {
        for (String inseguro : new String[] {"a\r\nSet-Cookie: x=1", "com espaco", "x".repeat(65), "<script>", " "}) {
            assertThat(ecoado(inseguro)).matches("[0-9a-f-]{36}");
        }
        assertThat(ecoado(null)).matches("[0-9a-f-]{36}");
    }
}
