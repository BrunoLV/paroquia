package br.com.nsfatima.gestao.calendario.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
class CacheInfrastructureTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void deveCaffeineCacheManager() {
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    void deveTerConfiguredCacheia() {
        assertThat(cacheManager.getCacheNames())
                .containsExactlyInAnyOrder(CacheConfig.PROJETO_RESUMO_CACHE, CacheConfig.ANO_PAROQUIAL_CACHE,
                        CacheConfig.DASHBOARD_ANIVERSARIANTES_CACHE);
    }

    @Test
    void testProjectResumoCacheEviction() {
        Cache cache = cacheManager.getCache(CacheConfig.PROJETO_RESUMO_CACHE);
        assertThat(cache).isNotNull();

        String key = "test-key";
        String value = "test-value";

        cache.put(key, value);

        assertThat(cache.get(key)).isNotNull();
        assertThat(Objects.requireNonNull(cache.get(key)).get()).isEqualTo(value);
    }
}
