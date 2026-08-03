package br.com.nsfatima.gestao.calendario.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configura o cache em memória (Caffeine) usado pelo contexto de calendário para evitar
 * recomputações caras de leituras frequentes, como o resumo de projetos e o ano paroquial vigente.
 * As entradas expiram automaticamente após um curto período para não servir dados desatualizados.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Nome do cache que guarda o resumo agregado de projetos. */
    public static final String PROJECT_RESUMO_CACHE = "projectResumo";
    /** Nome do cache que guarda os dados do ano paroquial corrente. */
    public static final String ANO_PAROQUIAL_CACHE = "anoParoquial";

    /**
     * Cria o {@link CacheManager} baseado em Caffeine com política de expiração e capacidade
     * compartilhada por todos os caches deste contexto.
     *
     * @return gerenciador de cache configurado com capacidade inicial de 100, máximo de 500
     *     entradas e expiração de 10 minutos após escrita
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(PROJECT_RESUMO_CACHE, ANO_PAROQUIAL_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES));
        return cacheManager;
    }
}
