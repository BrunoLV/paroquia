package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AnoLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiturgicalProvisioningServiceTest {

    @Mock
    private AnoLiturgicoRepository anoLiturgicoRepository;

    @Mock
    private CicloLiturgicoRepository cicloLiturgicoRepository;

    private LiturgicalEngine liturgicalEngine;
    private LiturgicalProvisioningService service;

    @BeforeEach
    void setUp() {
        liturgicalEngine = new LiturgicalEngine();
        service = new LiturgicalProvisioningService(anoLiturgicoRepository, cicloLiturgicoRepository, liturgicalEngine);
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
    @DisplayName("Deve criar e provisionar automaticamente o Ano Litúrgico e seus 6 ciclos quando não existir (ex: 2036)")
    void testShouldProvisionAnoLiturgicoAndCyclesAutomaticallyWhenMissing() {
        when(anoLiturgicoRepository.findById(2036)).thenReturn(Optional.empty());
        when(anoLiturgicoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AnoLiturgicoEntity result = service.ensureAnoLiturgicoEExiste(2036);

        assertThat(result).isNotNull();
        assertThat(result.getAno()).isEqualTo(2036);
        assertThat(result.getCicloLetra()).isEqualTo("A"); // 2036 é Ano A

        verify(anoLiturgicoRepository).save(any(AnoLiturgicoEntity.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CicloLiturgicoEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(cicloLiturgicoRepository).saveAll(captor.capture());

        List<CicloLiturgicoEntity> savedCycles = captor.getValue();
        assertThat(savedCycles).hasSize(6);
        assertThat(savedCycles).extracting(CicloLiturgicoEntity::getAnoLiturgicoRef).containsOnly(2036);
    }
}
