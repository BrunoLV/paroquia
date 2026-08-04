package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AnoLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.SolenidadeLiturgicaJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class LiturgicalProvisioningServiceTest {

    @Mock
    private AnoLiturgicoRepository anoLiturgicoRepository;

    @Mock
    private CicloLiturgicoRepository cicloLiturgicoRepository;

    @Mock
    private EventoJpaRepository eventoJpaRepository;

    @Mock
    private OrganizacaoJpaRepository organizacaoJpaRepository;

    @Mock
    private SolenidadeLiturgicaJpaRepository solenidadeLiturgicaJpaRepository;

    private MotorLiturgico liturgicalEngine;
    private ProvisionamentoLiturgicoService service;

    @BeforeEach
    void setUp() {
        liturgicalEngine = new MotorLiturgico();
        service = new ProvisionamentoLiturgicoService(
                anoLiturgicoRepository,
                cicloLiturgicoRepository,
                liturgicalEngine,
                eventoJpaRepository,
                organizacaoJpaRepository,
                solenidadeLiturgicaJpaRepository
        );
    }

    @Test
    @DisplayName("Deve retornar Ano Litúrgico existente sem criar novamente caso já esteja cadastrado")
    void testShouldReturnExistingAnoLiturgicoWhenAlreadyPresent() {
        AnoLiturgicoEntity existing = new AnoLiturgicoEntity(2026, "C", Instant.now(), Instant.now(), Instant.now());
        when(anoLiturgicoRepository.findById(2026)).thenReturn(Optional.of(existing));

        AnoLiturgicoEntity result = service.ensureAnoLiturgicoEExiste(2026);

        assertThat(result).isNotNull();
        assertThat(result.getAno()).isEqualTo(2026);
        verify(anoLiturgicoRepository, never()).save(any());
        verify(cicloLiturgicoRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Deve criar e provisionar automaticamente o Ano Litúrgico, ciclos e eventos de Solenidade na Matriz e Comunidades")
    void testShouldProvisionAnoLiturgicoCyclesAndSolemnityEventsWhenMissing() {
        when(anoLiturgicoRepository.findById(2036)).thenReturn(Optional.empty());
        when(anoLiturgicoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrganizacaoEntity matriz = new OrganizacaoEntity();
        matriz.setId(UUID.randomUUID());
        matriz.setNome("Paróquia Matriz");
        matriz.setTipo(TipoOrganizacao.MATRIZ);

        OrganizacaoEntity comunidade = new OrganizacaoEntity();
        comunidade.setId(UUID.randomUUID());
        comunidade.setNome("Comunidade São José");
        comunidade.setTipo(TipoOrganizacao.COMUNIDADE);

        when(organizacaoJpaRepository.findAll()).thenReturn(List.of(matriz, comunidade));

        AnoLiturgicoEntity result = service.ensureAnoLiturgicoEExiste(2036);

        assertThat(result).isNotNull();
        assertThat(result.getAno()).isEqualTo(2036);
        assertThat(result.getCicloLetra()).isEqualTo("B"); // 2036 é Ano B (2036 % 3 == 2)

        verify(anoLiturgicoRepository).save(any(AnoLiturgicoEntity.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CicloLiturgicoEntity>> captorCiclos = ArgumentCaptor.forClass(List.class);
        verify(cicloLiturgicoRepository).saveAll(captorCiclos.capture());

        List<CicloLiturgicoEntity> savedCycles = captorCiclos.getValue();
        assertThat(savedCycles).hasSize(6);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EventoEntity>> captorEventos = ArgumentCaptor.forClass(List.class);
        verify(eventoJpaRepository).saveAll(captorEventos.capture());

        List<EventoEntity> savedEvents = captorEventos.getValue();
        // 21 marcos (9 solenidades fixas + 12 marcos móveis) x 2 organizações (Matriz + Comunidade) = 42 eventos
        assertThat(savedEvents).hasSize(42);
        assertThat(savedEvents).extracting(EventoEntity::getCategoria).containsOnly(CategoriaEvento.LITURGICO);
        assertThat(savedEvents).extracting(EventoEntity::getStatus).containsOnly("CONFIRMADO");
    }
}
