package br.com.nsfatima.gestao.governanca.domain.service;

import br.com.nsfatima.gestao.governanca.domain.repository.AnoParoquialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SucessaoAnoParoquialServiceTest {

    @Mock
    private AnoParoquialRepository repository;

    @Test
    void bloqueiaQuandoExisteAnoAnteriorNaoConcluido() {
        when(repository.existeAnoAnteriorNaoConcluido(2026)).thenReturn(true);
        assertThatThrownBy(() -> new SucessaoAnoParoquialService(repository).assegurarAnosAnterioresConcluidos(2026))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void liberaQuandoTodosOsAnosAnterioresEstaoConcluidos() {
        when(repository.existeAnoAnteriorNaoConcluido(2026)).thenReturn(false);
        assertThatCode(() -> new SucessaoAnoParoquialService(repository).assegurarAnosAnterioresConcluidos(2026))
                .doesNotThrowAnyException();
    }
}
