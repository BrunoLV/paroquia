package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** O cache de aniversariantes nao pode servir ontem como se fosse hoje apos a virada do dia de Brasilia. */
@SpringJUnitConfig(ListarAniversariantesCacheTest.Config.class)
class ListarAniversariantesCacheTest {

    private static final AtomicReference<Instant> AGORA = new AtomicReference<>();
    private static final FielJpaRepository FIEIS = mock(FielJpaRepository.class);

    @Autowired
    private ListarAniversariantesUseCase useCase;

    @Configuration
    @EnableCaching
    static class Config {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(ListarAniversariantesUseCase.CACHE);
        }

        @Bean
        RelogioNegocio relogioNegocio() {
            Clock movel = new Clock() {
                @Override
                public ZoneId getZone() {
                    return ZoneOffset.UTC;
                }

                @Override
                public Clock withZone(ZoneId zone) {
                    return this;
                }

                @Override
                public Instant instant() {
                    return AGORA.get();
                }
            };
            return new RelogioNegocio(movel, ZoneId.of("America/Sao_Paulo"));
        }

        @Bean
        ListarAniversariantesUseCase listarAniversariantesUseCase(RelogioNegocio relogioNegocio) {
            return new ListarAniversariantesUseCase(FIEIS, relogioNegocio);
        }
    }

    @Test
    @DisplayName("Resultado em cache de ontem nao e servido depois da meia-noite de Brasilia")
    void cacheVirandoODia() {
        FielEntity fiel = new FielEntity();
        fiel.setNome("Maria Souza");
        fiel.setAtivo(true);
        fiel.setPermitirDivulgacaoAniversario(true);
        fiel.setDataNascimento(LocalDate.of(1990, 7, 1));
        when(FIEIS.findByAtivoTrue()).thenReturn(List.of(fiel));

        AGORA.set(Instant.parse("2026-07-01T01:30:00Z")); // 30/06 22h30 BRT
        assertThat(useCase.execute(PeriodoDashboard.HOJE)).isEmpty();

        AGORA.set(Instant.parse("2026-07-01T04:00:00Z")); // 01/07 01h BRT
        assertThat(useCase.execute(PeriodoDashboard.HOJE)).hasSize(1);
    }
}
