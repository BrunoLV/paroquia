package br.com.nsfatima.gestao.projeto.application.service;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SpringBootTest
class ProjetoAgregacaoCacheIntegrationTest {

    @Autowired
    private ProjetoAgregacaoService service;

    @MockitoBean
    private ProjetoEventoJpaRepository projetoRepository;

    @MockitoBean
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
