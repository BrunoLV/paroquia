package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SincronizarStatusReservadoAtivoUseCaseTest {

    @Mock private AtivoPatrimonialRepository repository;
    @Mock private ConsultaReservaDeAtivoPort consultaReservaPort;

    private final UUID ativoId = UUID.randomUUID();

    private SincronizarStatusReservadoAtivoUseCase useCase() {
        return new SincronizarStatusReservadoAtivoUseCase(repository, consultaReservaPort);
    }

    private AtivoPatrimonial ativo(AtivoStatus status) {
        return new AtivoPatrimonial(new AtivoPatrimonialId(ativoId), "Projetor", CodigoTombamento.de(2026, 1), null,
                AtivoCategoria.ELETRONICO, LocalDate.now(), BigDecimal.TEN, UUID.randomUUID(), true, status,
                UUID.randomUUID(), Instant.now(), Instant.now(), null, 0L);
    }

    @Test
    void marcaReservadoQuandoHaReservaAtiva() {
        when(repository.findById(new AtivoPatrimonialId(ativoId)))
                .thenReturn(Optional.of(ativo(AtivoStatus.DISPONIVEL)));
        when(consultaReservaPort.temReservaAtivaAgora(ativoId)).thenReturn(true);

        useCase().execute(ativoId);

        ArgumentCaptor<AtivoPatrimonial> captor = ArgumentCaptor.forClass(AtivoPatrimonial.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AtivoStatus.RESERVADO);
    }

    @Test
    void ignoraAtivoEmManutencao() {
        when(repository.findById(new AtivoPatrimonialId(ativoId)))
                .thenReturn(Optional.of(ativo(AtivoStatus.EM_MANUTENCAO)));
        useCase().execute(ativoId);
        verify(repository, never()).save(any());
    }
}
