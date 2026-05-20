package br.com.nsfatima.gestao.calendario.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

@SpringBootTest
class CacheInfrastructureTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldBeCaffeineCacheManager() {
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    void shouldHaveConfiguredCaches() {
        assertThat(cacheManager.getCacheNames())
                .containsExactlyInAnyOrder(CacheConfig.PROJECT_RESUMO_CACHE, CacheConfig.ANO_PAROQUIAL_CACHE);
    }

    @Test
    void shouldStoreAndRetrieveFromCache() {
        Cache cache = cacheManager.getCache(CacheConfig.PROJECT_RESUMO_CACHE);
        assertThat(cache).isNotNull();

        String key = "test-key";
        String value = "test-value";

        cache.put(key, value);
        
        assertThat(cache.get(key)).isNotNull();
        assertThat(cache.get(key).get()).isEqualTo(value);
    }
}
