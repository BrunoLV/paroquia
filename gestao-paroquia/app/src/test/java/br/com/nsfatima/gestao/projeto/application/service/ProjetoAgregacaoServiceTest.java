package br.com.nsfatima.gestao.projeto.application.service;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNaoEncontradoException;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.projeto.domain.service.ProvedorEventoProjeto;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ProjetoAgregacaoServiceTest {

    @Mock
    private ProjetoEventoJpaRepository projetoRepository;

    @Mock
    private ProvedorEventoProjeto provedorEventoProjeto;

    @Mock
    private ProjetoAgregacaoDomainService domainService;

    @InjectMocks
    private ProjetoAgregacaoService service;

    @Test
    @DisplayName("Deve retornar resumo do projeto corretamente")
    void deveRetornarResumoDoProjeto() {
        UUID projetoId = UUID.randomUUID();
        Instant agora = Instant.now();

        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(projetoId);
        projeto.setInicioUtc(agora.minus(10, ChronoUnit.DAYS));
        projeto.setFimUtc(agora.plus(10, ChronoUnit.DAYS));

        when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));
        when(provedorEventoProjeto.contarPorProjetoId(projetoId)).thenReturn(10L);
        when(provedorEventoProjeto.contarPorProjetoIdEFimUtcMenorQue(eq(projetoId), any(Instant.class))).thenReturn(6L);
        when(provedorEventoProjeto.contarPorProjetoIdEFimUtcMaiorOuIgualA(eq(projetoId), any(Instant.class)))
                .thenReturn(4L);
        when(provedorEventoProjeto.buscarNomesOrganizacoesEnvolvidas(projetoId)).thenReturn(List.of("Org 1", "Org 2"));

        when(domainService.calcularPercentualTempoDecorrido(any(), any())).thenReturn(BigDecimal.valueOf(50.0));
        when(domainService.verificarSeEstaEmRisco(any(), any(), anyInt())).thenReturn(false);

        ProjetoResumoDTO resumo = service.obterResumo(projetoId);

        assertNotNull(resumo);
        assertEquals(10, resumo.statusExecucao().totalEventos());
        assertEquals(6, resumo.statusExecucao().eventosRealizados());
        assertEquals(4, resumo.statusExecucao().eventosPendentes());
        assertThat(resumo.mapaColaboracao().envolvidos()).containsExactlyInAnyOrder("Org 1", "Org 2");
        assertEquals(BigDecimal.valueOf(50.0), resumo.saudeTemporal().percentualTempoDecorrido());
        assertFalse(resumo.saudeTemporal().emRisco());
    }

    @Test
    @DisplayName("Deve lançar exceção quando projeto não encontrado")
    void deveLancarexcecaoQuandoProjetoNaoEncontrado() {
        UUID projetoId = UUID.randomUUID();
        when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

        assertThrows(ProjetoNaoEncontradoException.class, () -> service.obterResumo(projetoId));
    }
}
