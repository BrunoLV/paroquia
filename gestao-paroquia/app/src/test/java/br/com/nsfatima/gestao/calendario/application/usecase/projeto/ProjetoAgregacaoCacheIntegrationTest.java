package br.com.nsfatima.gestao.projeto.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

@SpringBootTest
class ProjetoAgregacaoCacheIntegrationTest {

    @Autowired
    private ProjetoAgregacaoService service;

    @MockBean
    private ProjetoEventoJpaRepository projetoRepository;

    @MockBean
    private EventoJpaRepository eventoRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCacheProjectResumo() {
        UUID projetoId = UUID.randomUUID();
        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(projetoId);
        entity.setInicioUtc(Instant.now());
        entity.setFimUtc(Instant.now().plusSeconds(3600));

        when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(entity));
        
        // Ensure cache is clear
        cacheManager.getCache("projectResumo").clear();

        // First call - should hit repository
        service.obterResumo(projetoId);
        verify(projetoRepository, times(1)).findById(projetoId);

        // Second call - should HIT CACHE (repository not called again)
        service.obterResumo(projetoId);
        verify(projetoRepository, times(1)).findById(projetoId);
    }
}
