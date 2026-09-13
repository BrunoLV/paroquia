package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.dto.DisponibilidadeFaixaDto;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort.JanelaIndisponivel;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObterDisponibilidadeItemUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ConsultaManutencaoAtivoPort consultaManutencaoPort;

    private ObterDisponibilidadeItemUseCase useCase() {
        return new ObterDisponibilidadeItemUseCase(reservaRepository, consultaManutencaoPort);
    }

    @Test
    @DisplayName("Deve exigir tipoItem e itemId não nulos")
    void deveExigirParametrosObrigatorios() {
        assertThatThrownBy(() -> useCase().executar(null, UUID.randomUUID(), LocalDate.now()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> useCase().executar(TipoItemReserva.LOCAL, null, LocalDate.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Deve retornar apenas faixas de reservas que ocupam agenda para LOCAL, sem consultar manutenção")
    void deveRetornarFaixasDeLocalSemManutencao() {
        UUID itemId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        Reserva ativa = Reserva.criar(ReservaId.generate(), TipoItemReserva.LOCAL, itemId, intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), null, null, StatusReserva.CONFIRMADA, Instant.now());
        Reserva cancelada = Reserva.criar(ReservaId.generate(), TipoItemReserva.LOCAL, itemId, intervalo, "desc2",
                UUID.randomUUID(), UUID.randomUUID(), null, null, StatusReserva.PENDENTE, Instant.now());
        cancelada.cancelar(UUID.randomUUID(), "motivo qualquer suficientemente longo", false, Instant.now());

        when(reservaRepository.findAll(any(ReservaFiltro.class))).thenReturn(List.of(ativa, cancelada));

        List<DisponibilidadeFaixaDto> resultado = useCase().executar(TipoItemReserva.LOCAL, itemId, LocalDate.now());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).reservaId()).isEqualTo(ativa.getId().value());
        verify(consultaManutencaoPort, never()).janelasDeManutencao(any(), any(), any());
    }

    @Test
    @DisplayName("Deve incluir janelas de manutenção quando tipoItem é ATIVO")
    void deveIncluirJanelasDeManutencaoParaAtivo() {
        UUID itemId = UUID.randomUUID();
        when(reservaRepository.findAll(any(ReservaFiltro.class))).thenReturn(List.of());
        Instant inicioJanela = Instant.now();
        Instant fimJanela = inicioJanela.plusSeconds(3600);
        when(consultaManutencaoPort.janelasDeManutencao(eq(itemId), any(), any()))
                .thenReturn(List.of(new JanelaIndisponivel(inicioJanela, fimJanela)));

        List<DisponibilidadeFaixaDto> resultado = useCase().executar(TipoItemReserva.ATIVO, itemId, LocalDate.now());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).responsavel()).isEqualTo("Em manutenção");
        assertThat(resultado.get(0).reservaId()).isNull();
    }

    @Test
    @DisplayName("Deve usar data atual quando data não é informada")
    void deveUsarDataAtualQuandoNaoInformada() {
        UUID itemId = UUID.randomUUID();
        when(reservaRepository.findAll(any(ReservaFiltro.class))).thenReturn(List.of());

        List<DisponibilidadeFaixaDto> resultado = useCase().executar(TipoItemReserva.LOCAL, itemId, null);

        assertThat(resultado).isEmpty();
    }
}
