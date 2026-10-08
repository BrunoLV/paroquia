package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnriquecedorReservaServiceTest {

    @Mock private LocalJpaRepository localRepository;
    @Mock private AtivoPatrimonialJpaRepository ativoRepository;
    @Mock private OrganizacaoJpaRepository organizacaoRepository;
    @Mock private UsuarioJpaRepository usuarioRepository;
    @Mock private EventoJpaRepository eventoRepository;

    private EnriquecedorReservaService service() {
        return new EnriquecedorReservaService(
                localRepository, ativoRepository, organizacaoRepository, usuarioRepository, eventoRepository);
    }

    private Reserva reservaLocal(UUID itemId, UUID solicitanteId, UUID orgId, UUID eventoId) {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 15);
        return Reserva.criar(
                ReservaId.gerar(), TipoItemReserva.LOCAL, itemId, intervalo, "Ensaio",
                solicitanteId, orgId, eventoId, null, StatusReserva.PENDENTE, Instant.now());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando reservas for nula ou vazia")
    void deveRetornarListaVaziaQuandoReservasNulaOuVazia() {
        assertThat(service().enriquecer((List<Reserva>) null)).isEmpty();
        assertThat(service().enriquecer(List.of())).isEmpty();
        verifyNoInteractions(
                localRepository, ativoRepository, organizacaoRepository, usuarioRepository, eventoRepository);
    }

    @Test
    @DisplayName("Deve retornar null ao enriquecer reserva única nula")
    void deveRetornarNullParaReservaUnicaNula() {
        assertThat(service().enriquecer((Reserva) null)).isNull();
    }

    @Test
    @DisplayName("Deve enriquecer reserva de LOCAL resolvendo nomes de local, solicitante, pastoral e evento")
    void deveEnriquecerReservaDeLocalComTodosOsRelacionamentos() {
        UUID localId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        Reserva reserva = reservaLocal(localId, solicitanteId, orgId, eventoId);

        LocalEntity local = new LocalEntity();
        local.setId(localId);
        local.setNome("Salão Paroquial");
        when(localRepository.findAllById(any())).thenReturn(List.of(local));

        UsuarioEntity solicitante = new UsuarioEntity();
        solicitante.setId(solicitanteId);
        solicitante.setUsername("joao.silva");
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(solicitante));

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(orgId);
        org.setNome("Pastoral da Liturgia");
        when(organizacaoRepository.findAllById(any())).thenReturn(List.of(org));

        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Missa Dominical");
        when(eventoRepository.findAllById(any())).thenReturn(List.of(evento));

        ReservaDetalheDto dto = service().enriquecer(reserva);

        assertThat(dto).isNotNull();
        assertThat(dto.itemNome()).isEqualTo("Salão Paroquial");
        assertThat(dto.solicitanteNome()).isEqualTo("joao.silva");
        assertThat(dto.pastoralMovimentoNome()).isEqualTo("Pastoral da Liturgia");
        assertThat(dto.eventoTitulo()).isEqualTo("Missa Dominical");
        assertThat(dto.id()).isEqualTo(reserva.getId().value());
        assertThat(dto.tipoItem()).isEqualTo(TipoItemReserva.LOCAL);
        assertThat(dto.status()).isEqualTo(StatusReserva.PENDENTE);

        verify(ativoRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Deve enriquecer reserva de ATIVO resolvendo nome do ativo patrimonial")
    void deveEnriquecerReservaDeAtivo() {
        UUID ativoId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.gerar(), TipoItemReserva.ATIVO, ativoId, intervalo, "Som",
                solicitanteId, orgId, null, null, StatusReserva.PENDENTE, Instant.now());

        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Caixa de Som JBL");
        when(ativoRepository.findAllById(any())).thenReturn(List.of(ativo));
        lenient().when(usuarioRepository.findAllById(any())).thenReturn(List.of());
        lenient().when(organizacaoRepository.findAllById(any())).thenReturn(List.of());

        ReservaDetalheDto dto = service().enriquecer(reserva);

        assertThat(dto.itemNome()).isEqualTo("Caixa de Som JBL");
        assertThat(dto.eventoId()).isNull();
        assertThat(dto.eventoTitulo()).isNull();
        verify(localRepository, never()).findAllById(any());
        verify(eventoRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Deve usar nomes de fallback quando entidades relacionadas não são encontradas")
    void deveUsarNomesFallbackQuandoNaoEncontrados() {
        UUID localId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        Reserva reserva = reservaLocal(localId, solicitanteId, orgId, null);

        when(localRepository.findAllById(any())).thenReturn(List.of());
        when(usuarioRepository.findAllById(any())).thenReturn(List.of());
        when(organizacaoRepository.findAllById(any())).thenReturn(List.of());

        ReservaDetalheDto dto = service().enriquecer(reserva);

        assertThat(dto.itemNome()).isEqualTo("Local " + localId);
        assertThat(dto.solicitanteNome()).isEqualTo("Usuário");
        assertThat(dto.pastoralMovimentoNome()).isEqualTo("Pastoral");
        verify(eventoRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Deve resolver nomes de aprovador e cancelador quando presentes")
    void deveResolverNomesDeAprovadorECancelador() {
        UUID localId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID aprovadorId = UUID.randomUUID();
        UUID canceladorId = UUID.randomUUID();

        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.gerar(), TipoItemReserva.LOCAL, localId, intervalo, "Reunião",
                solicitanteId, orgId, null, null, StatusReserva.PENDENTE, Instant.now());
        reserva.aprovar(aprovadorId, Instant.now());
        reserva.cancelar(canceladorId, "Necessidade de reagendamento por indisponibilidade", true, Instant.now());

        UsuarioEntity aprovador = new UsuarioEntity();
        aprovador.setId(aprovadorId);
        aprovador.setUsername("aprovador.user");
        UsuarioEntity cancelador = new UsuarioEntity();
        cancelador.setId(canceladorId);
        cancelador.setUsername("cancelador.user");
        UsuarioEntity solicitante = new UsuarioEntity();
        solicitante.setId(solicitanteId);
        solicitante.setUsername("solicitante.user");

        when(usuarioRepository.findAllById(any())).thenReturn(List.of(aprovador, cancelador, solicitante));
        when(localRepository.findAllById(any())).thenReturn(List.of());
        when(organizacaoRepository.findAllById(any())).thenReturn(List.of());

        ReservaDetalheDto dto = service().enriquecer(reserva);

        assertThat(dto.aprovadorNome()).isEqualTo("aprovador.user");
        assertThat(dto.canceladoPorNome()).isEqualTo("cancelador.user");
        assertThat(dto.motivoCancelamento()).isEqualTo("Necessidade de reagendamento por indisponibilidade");
    }

    @Test
    @DisplayName("Deve enriquecer lista com múltiplas reservas sem duplicar buscas por id")
    void deveEnriquecerListaComMultiplasReservas() {
        UUID localId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        Reserva r1 = reservaLocal(localId, solicitanteId, orgId, null);
        Reserva r2 = reservaLocal(localId, solicitanteId, orgId, null);

        when(localRepository.findAllById(any())).thenReturn(List.of());
        when(usuarioRepository.findAllById(any())).thenReturn(List.of());
        when(organizacaoRepository.findAllById(any())).thenReturn(List.of());

        List<ReservaDetalheDto> dtos = service().enriquecer(List.of(r1, r2));

        assertThat(dtos).hasSize(2);
    }
}
