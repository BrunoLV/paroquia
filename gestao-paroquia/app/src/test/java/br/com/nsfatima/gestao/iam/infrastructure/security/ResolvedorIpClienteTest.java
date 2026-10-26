package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ResolvedorIpClienteTest {

    private MockHttpServletRequest requisicao(String remoto, String xff) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoto);
        if (xff != null) {
            request.addHeader("X-Forwarded-For", xff);
        }
        return request;
    }

    @Test
    @DisplayName("Padrão: ignora X-Forwarded-For e usa apenas o endereço da conexão")
    void padraoIgnoraCabecalho() {
        assertThat(new ResolvedorIpCliente("").resolver(requisicao("10.0.0.5", "6.6.6.6"))).isEqualTo("10.0.0.5");
    }

    @Test
    @DisplayName("Conexão de proxy confiável: usa o último salto não confiável")
    void proxyConfiavelUsaCabecalho() {
        ResolvedorIpCliente resolvedor = new ResolvedorIpCliente("10.0.0.5, 10.0.0.6");

        assertThat(resolvedor.resolver(requisicao("10.0.0.5", "203.0.113.9"))).isEqualTo("203.0.113.9");
        assertThat(resolvedor.resolver(requisicao("10.0.0.5", "6.6.6.6, 203.0.113.9, 10.0.0.6")))
                .isEqualTo("203.0.113.9");
    }

    @Test
    @DisplayName("Conexão de origem não confiável: cabeçalho forjado é ignorado")
    void origemNaoConfiavelIgnoraCabecalho() {
        ResolvedorIpCliente resolvedor = new ResolvedorIpCliente("10.0.0.5");

        assertThat(resolvedor.resolver(requisicao("198.51.100.7", "6.6.6.6"))).isEqualTo("198.51.100.7");
    }
}
