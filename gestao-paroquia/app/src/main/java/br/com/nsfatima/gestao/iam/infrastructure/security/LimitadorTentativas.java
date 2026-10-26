package br.com.nsfatima.gestao.iam.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contador de tentativas por chave com janela fixa, usado como defesa contra força bruta no login e
 * contra abuso do pedido público de redefinição de senha. A janela começa na primeira tentativa da
 * chave e expira sozinha (sem lockout permanente que viabilizasse negação de serviço contra uma
 * conta). A chave combina IP de origem e usuário normalizado, de modo que um atacante não bloqueie
 * o titular legítimo conectado de outro endereço. O estado é local ao processo (Caffeine): com
 * várias instâncias da API o limite efetivo é multiplicado pelo número de réplicas.
 */
public class LimitadorTentativas {

    private static final long MAXIMO_CHAVES_RASTREADAS = 100_000;

    private final int maxTentativas;
    private final Duration janelaExpiracao;
    private final Cache<String, AtomicInteger> tentativas;

    public LimitadorTentativas(int maxTentativas, Duration janela, Ticker ticker) {
        this.maxTentativas = maxTentativas;
        this.janelaExpiracao = janela;
        this.tentativas = Caffeine.newBuilder()
                .expireAfterWrite(janela)
                .maximumSize(MAXIMO_CHAVES_RASTREADAS)
                .ticker(ticker)
                .build();
    }

    /**
     * Compõe a chave de limitação, normalizando o usuário para que variações de caixa e espaços não
     * contornem o contador.
     */
    public static String chave(String ip, String username) {
        String usuario = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return (ip == null ? "desconhecido" : ip) + "|" + usuario;
    }

    public boolean bloqueado(String chave) {
        AtomicInteger contador = tentativas.getIfPresent(chave);
        return contador != null && contador.get() >= maxTentativas;
    }

    public void registrarTentativa(String chave) {
        tentativas.get(chave, k -> new AtomicInteger()).incrementAndGet();
    }

    public void limpar(String chave) {
        tentativas.invalidate(chave);
    }

    public Duration janela() {
        return janelaExpiracao;
    }
}
