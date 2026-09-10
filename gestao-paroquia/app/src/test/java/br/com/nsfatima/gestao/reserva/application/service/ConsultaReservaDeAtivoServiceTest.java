package br.com.nsfatima.gestao.reserva.application.service;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.repository.ReservaJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaReservaDeAtivoServiceTest {

    @Mock private ReservaJpaRepository reservaRepository;
    @Mock private UsuarioJpaRepository usuarioRepository;

    private ConsultaReservaDeAtivoService service() {
        return new ConsultaReservaDeAtivoService(reservaRepository, usuarioRepository);
    }

    @Test
    void temReservaAtivaAgoraDelegaAoRepositorio() {
        UUID ativoId = UUID.randomUUID();
        when(reservaRepository.existeReservaAtivaNoInstante(eq(ativoId), any())).thenReturn(true);
        assertThat(service().temReservaAtivaAgora(ativoId)).isTrue();
    }

    @Test
    void reservasImpeditivasResolveNomeDoSolicitante() {
        UUID ativoId = UUID.randomUUID();
        UUID solicitante = UUID.randomUUID();
        Instant ini = Instant.now();
        Instant fim = ini.plus(2, ChronoUnit.DAYS);

        ReservaEntity r = new ReservaEntity(UUID.randomUUID(), "ATIVO", ativoId, ini, fim, 30,
                "Missa", solicitante, UUID.randomUUID(), null, null, "CONFIRMADA", null, null, null, null, null,
                ini, ini, null, 0L);
        when(reservaRepository.findImpeditivasParaManutencao(ativoId, ini, fim)).thenReturn(List.of(r));

        UsuarioEntity u = new UsuarioEntity();
        u.setId(solicitante);
        u.setUsername("maria.souza");
        lenient().when(usuarioRepository.findAllById(any())).thenReturn(List.of(u));

        List<ReservaImpeditivaView> views = service().reservasImpeditivas(ativoId, ini, fim);

        assertThat(views).hasSize(1);
        assertThat(views.get(0).solicitanteNome()).isEqualTo("maria.souza");
        assertThat(views.get(0).status()).isEqualTo("CONFIRMADA");
    }
}
