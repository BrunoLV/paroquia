package br.com.nsfatima.gestao.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Determina o IP do cliente usado como parte da chave do limite de tentativas. Por padrão confia
 * somente em {@code getRemoteAddr()}: {@code X-Forwarded-For} é controlado pelo cliente e, aceito
 * sem critério, permitiria burlar o limite trocando o cabeçalho a cada tentativa. Atrás de proxy
 * reverso, liste os IPs dos proxies em {@code app.security.rate-limit.trusted-proxies}; o cabeçalho
 * só é lido quando a conexão vem de um deles, percorrido da direita para a esquerda até o primeiro
 * endereço não confiável (o último salto que um proxy confiável realmente observou).
 */
@Component
public class ResolvedorIpCliente {

    private static final String CABECALHO = "X-Forwarded-For";

    private final Set<String> proxiesConfiaveis;

    public ResolvedorIpCliente(@Value("${app.security.rate-limit.trusted-proxies:}") String proxiesConfiaveis) {
        this.proxiesConfiaveis = Arrays.stream(proxiesConfiaveis.split(","))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public String resolver(HttpServletRequest request) {
        String remoto = request.getRemoteAddr();
        String encaminhado = request.getHeader(CABECALHO);
        if (encaminhado == null || !proxiesConfiaveis.contains(remoto)) {
            return remoto;
        }
        String[] saltos = encaminhado.split(",");
        for (int i = saltos.length - 1; i >= 0; i--) {
            String salto = saltos[i].trim();
            if (!salto.isEmpty() && !proxiesConfiaveis.contains(salto)) {
                return salto;
            }
        }
        return remoto;
    }
}
