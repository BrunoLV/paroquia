package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.application.service.GeradorNumeroTombamento;
import br.com.nsfatima.gestao.patrimonio.application.service.PatrimonioNumeracaoAnualSeeder;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.PatrimonioNumeracaoAnualEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.PatrimonioNumeracaoAnualJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeradorNumeroTombamentoTest {

    @Mock
    private PatrimonioNumeracaoAnualJpaRepository repository;

    @Mock
    private PatrimonioNumeracaoAnualSeeder seeder;

    private GeradorNumeroTombamento gerador;

    @BeforeEach
    void setUp() {
        gerador = new GeradorNumeroTombamento(repository, seeder);
    }

    @Test
    void deveGerarPrimeiroNumeroDoAno() {
        PatrimonioNumeracaoAnualEntity contador = new PatrimonioNumeracaoAnualEntity();
        contador.setAno(2026);
        contador.setUltimoNumero(0);
        when(repository.findById(2026)).thenReturn(Optional.of(contador));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String numero = gerador.gerar(2026);

        assertThat(numero).isEqualTo("PAT-2026-0001");
        verify(seeder).garantirLinhaDoAno(2026);
    }

    @Test
    void deveIncrementarSequencialmenteDentroDoMesmoAno() {
        PatrimonioNumeracaoAnualEntity contador = new PatrimonioNumeracaoAnualEntity();
        contador.setAno(2026);
        contador.setUltimoNumero(41);
        when(repository.findById(2026)).thenReturn(Optional.of(contador));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String numero = gerador.gerar(2026);

        assertThat(numero).isEqualTo("PAT-2026-0042");
        verify(seeder).garantirLinhaDoAno(2026);
    }
}
